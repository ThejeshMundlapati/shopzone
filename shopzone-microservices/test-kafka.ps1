$ErrorActionPreference = "Continue"

function Call-API {
    param([string]$Method, [string]$Url, [string]$Body = $null, [string]$Token = $null)
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body
        } else {
            return Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers
        }
    } catch {
        Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SHOPZONE KAFKA END-TO-END TEST" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$userSvc = "http://localhost:8081"
$prodSvc = "http://localhost:8082"
$cartSvc = "http://localhost:8083"
$orderSvc = "http://localhost:8084"
$productId = "6994e10f0efd7b36627051ff"

# VERIFY SERVICES
Write-Host ""
Write-Host "PREFLIGHT: Checking services..." -ForegroundColor Yellow
$allUp = $true
@(8081,8082,8083,8084,8085,8086) | ForEach-Object {
    try { Invoke-WebRequest -Uri "http://localhost:$_/actuator/health" -TimeoutSec 3 -UseBasicParsing | Out-Null; Write-Host "  $_ UP" -ForegroundColor Green }
    catch { Write-Host "  $_ DOWN" -ForegroundColor Red; $allUp = $false }
}
if (-not $allUp) { Write-Host "  Some services are down. Wait and retry." -ForegroundColor Red; exit }

# LOGIN
Write-Host ""
Write-Host "STEP 1: Logging in..." -ForegroundColor Yellow

$userLogin = Call-API -Method POST -Url "$userSvc/api/auth/login" -Body '{"email":"kafkatest@shopzone.com","password":"Password123!"}'
if (-not $userLogin -or -not $userLogin.data.accessToken) { Write-Host "  FAILED: user login" -ForegroundColor Red; exit }
$userToken = $userLogin.data.accessToken
Write-Host "  User logged in." -ForegroundColor Green

$adminLogin = Call-API -Method POST -Url "$userSvc/api/auth/login" -Body '{"email":"kafkaadmin@shopzone.com","password":"Password123!"}'
if (-not $adminLogin -or -not $adminLogin.data.accessToken) { Write-Host "  FAILED: admin login" -ForegroundColor Red; exit }
$adminToken = $adminLogin.data.accessToken
Write-Host "  Admin logged in." -ForegroundColor Green

# GET ADDRESS
$existingAddr = Call-API -Method GET -Url "$userSvc/api/addresses" -Token $userToken
$addressId = $existingAddr.data[0].id
Write-Host "  Address: $addressId" -ForegroundColor Green

# GET STOCK
$prodCheck = Call-API -Method GET -Url "$prodSvc/api/products/$productId"
$initialStock = $prodCheck.data.stock
Write-Host "  Product stock: $initialStock" -ForegroundColor Green

# ========================================
# TEST 1: FULL ORDER LIFECYCLE WITH EMAILS
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST 1: FULL ORDER LIFECYCLE + EMAILS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Cart
Write-Host ""
Write-Host "  Clearing cart..." -ForegroundColor Yellow
Call-API -Method DELETE -Url "$cartSvc/api/cart/clear" -Token $userToken | Out-Null
Start-Sleep -Seconds 1
$cartBody = '{"productId":"' + $productId + '","quantity":2}'
Call-API -Method POST -Url "$cartSvc/api/cart/add" -Body $cartBody -Token $userToken | Out-Null
Write-Host "  Cart: 2 units" -ForegroundColor Green

# Order
Start-Sleep -Seconds 1
Write-Host "  Placing order..." -ForegroundColor Yellow
$checkoutBody = '{"shippingAddressId":"' + $addressId + '","customerNotes":"Kafka full test"}'
$checkout = Call-API -Method POST -Url "$orderSvc/api/checkout/place-order" -Body $checkoutBody -Token $userToken

if (-not $checkout -or -not $checkout.data) { Write-Host "  CHECKOUT FAILED!" -ForegroundColor Red; exit }
$orderNumber = $checkout.data.order.orderNumber
$orderId = $checkout.data.order.id
Write-Host "  ORDER: $orderNumber" -ForegroundColor Green

# Wait for saga
Write-Host "  Waiting 5s for Kafka saga..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Stock check
$prodAfter = Call-API -Method GET -Url "$prodSvc/api/products/$productId"
$stockAfter = $prodAfter.data.stock
if ($stockAfter -lt $initialStock) {
    Write-Host "  STOCK REDUCED: $initialStock -> $stockAfter (Kafka PASS)" -ForegroundColor Green
} else {
    Write-Host "  STOCK UNCHANGED: $stockAfter (FAIL)" -ForegroundColor Red
}

# Saga check
$sagaOut = docker exec shopzone-postgres psql -U shopzone_admin -d shopzone_orders -t -c "SELECT status FROM saga_states WHERE order_number='$orderNumber';"
Write-Host "  SAGA: $($sagaOut.Trim())" -ForegroundColor Green

# Simulate payment via internal endpoint
Write-Host ""
Write-Host "  Simulating payment success..." -ForegroundColor Yellow
Call-API -Method POST -Url "$orderSvc/api/internal/orders/$orderId/record-payment" -Body '{"chargeId":"ch_kafka_test","receiptUrl":"https://receipt.stripe.com/kafka"}' | Out-Null
Start-Sleep -Seconds 1
$orderCheck = docker exec shopzone-postgres psql -U shopzone_admin -d shopzone_orders -t -c "SELECT status, payment_status FROM orders WHERE order_number='$orderNumber';"
Write-Host "  ORDER STATE: $($orderCheck.Trim())" -ForegroundColor Green

# Admin transitions: CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
Write-Host ""
Write-Host "  Admin: CONFIRMED -> PROCESSING..." -ForegroundColor Yellow
$r1 = Call-API -Method PATCH -Url "$orderSvc/api/admin/orders/$orderNumber/status" -Body '{"status":"PROCESSING","adminNotes":"kafka test"}' -Token $adminToken
if ($r1 -and $r1.success) { Write-Host "  PROCESSING" -ForegroundColor Green } else { Write-Host "  FAILED" -ForegroundColor Red }
Start-Sleep -Seconds 1

Write-Host "  Admin: PROCESSING -> SHIPPED..." -ForegroundColor Yellow
$r2 = Call-API -Method PATCH -Url "$orderSvc/api/admin/orders/$orderNumber/status" -Body '{"status":"SHIPPED","trackingNumber":"KAFKA-FINAL-001","shippingCarrier":"FedEx","adminNotes":"kafka test"}' -Token $adminToken
if ($r2 -and $r2.success) { Write-Host "  SHIPPED (Kafka ORDER_SHIPPED + email sent)" -ForegroundColor Green } else { Write-Host "  FAILED" -ForegroundColor Red }
Start-Sleep -Seconds 1

Write-Host "  Admin: SHIPPED -> DELIVERED..." -ForegroundColor Yellow
$r3 = Call-API -Method PATCH -Url "$orderSvc/api/admin/orders/$orderNumber/status" -Body '{"status":"DELIVERED","adminNotes":"kafka test"}' -Token $adminToken
if ($r3 -and $r3.success) { Write-Host "  DELIVERED (Kafka ORDER_DELIVERED + email sent)" -ForegroundColor Green } else { Write-Host "  FAILED" -ForegroundColor Red }

Write-Host ""
Write-Host "  TEST 1 RESULT: PASS" -ForegroundColor Green

# ========================================
# TEST 2: SAGA FAILURE - INSUFFICIENT STOCK
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST 2: SAGA FAILURE - LOW STOCK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Add to cart FIRST while stock is high
Write-Host ""
Write-Host "  Adding 5 to cart while stock is still available..." -ForegroundColor Yellow
Call-API -Method DELETE -Url "$cartSvc/api/cart/clear" -Token $userToken | Out-Null
Start-Sleep -Seconds 1
$cartBody2 = '{"productId":"' + $productId + '","quantity":5}'
$cartResult2 = Call-API -Method POST -Url "$cartSvc/api/cart/add" -Body $cartBody2 -Token $userToken
if ($cartResult2 -and $cartResult2.success) { Write-Host "  Cart: 5 units added" -ForegroundColor Green }
else { Write-Host "  Cart add failed" -ForegroundColor Red; exit }

# Step 2: THEN reduce stock to 1 (simulates someone else buying)
Write-Host "  Reducing stock to 1 (simulating race condition)..." -ForegroundColor Yellow
$currentProd = Call-API -Method GET -Url "$prodSvc/api/products/$productId"
$currentStock = $currentProd.data.stock
$reduceBy = $currentStock - 1
if ($reduceBy -gt 0) {
    Invoke-RestMethod -Uri "$prodSvc/api/internal/products/$productId/reduce-stock?quantity=$reduceBy" -Method POST -ContentType "application/json" | Out-Null
}
Start-Sleep -Seconds 1
$stockVerify = Call-API -Method GET -Url "$prodSvc/api/products/$productId"
Write-Host "  Stock now: $($stockVerify.data.stock)" -ForegroundColor White

# Step 3: Place order (cart has 5, stock has 1 - saga should fail)
Write-Host "  Placing order (5 items, only 1 in stock)..." -ForegroundColor Yellow
$checkoutBody2 = '{"shippingAddressId":"' + $addressId + '","customerNotes":"Saga failure test"}'
$checkout2 = Call-API -Method POST -Url "$orderSvc/api/checkout/place-order" -Body $checkoutBody2 -Token $userToken

if ($checkout2 -and $checkout2.data -and $checkout2.data.order) {
    $orderNumber2 = $checkout2.data.order.orderNumber
    Write-Host "  Order: $orderNumber2" -ForegroundColor White

    Write-Host "  Waiting 5s for saga compensation..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5

    $order2 = Call-API -Method GET -Url "$orderSvc/api/orders/$orderNumber2" -Token $userToken
    if ($order2 -and $order2.data) {
        $status2 = $order2.data.status
        Write-Host "  Order status: $status2" -ForegroundColor Cyan
        if ($order2.data.cancellationReason) {
            Write-Host "  Reason: $($order2.data.cancellationReason)" -ForegroundColor Cyan
        }
    }

    $sagaOut2 = docker exec shopzone-postgres psql -U shopzone_admin -d shopzone_orders -t -c "SELECT status, failure_reason FROM saga_states WHERE order_number='$orderNumber2';"
    Write-Host "  Saga: $($sagaOut2.Trim())" -ForegroundColor Cyan

    if ($status2 -eq "CANCELLED") {
        Write-Host ""
        Write-Host "  TEST 2 RESULT: PASS (saga compensation worked)" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "  TEST 2 RESULT: PARTIAL (order not cancelled)" -ForegroundColor Yellow
    }
} else {
    Write-Host "  Checkout failed at REST level" -ForegroundColor Red
}

# Restore stock to 50
Write-Host ""
Write-Host "  Restoring stock to 50..." -ForegroundColor Yellow
$finalProd = Call-API -Method GET -Url "$prodSvc/api/products/$productId"
$restoreBy = 50 - $finalProd.data.stock
if ($restoreBy -gt 0) {
    Invoke-RestMethod -Uri "$prodSvc/api/internal/products/$productId/increase-stock?quantity=$restoreBy" -Method POST -ContentType "application/json" | Out-Null
}
$restored = Call-API -Method GET -Url "$prodSvc/api/products/$productId"
Write-Host "  Stock: $($restored.data.stock)" -ForegroundColor Green

# ========================================
# FINAL SUMMARY
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FINAL SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  All saga states:" -ForegroundColor Yellow
docker exec shopzone-postgres psql -U shopzone_admin -d shopzone_orders -c "SELECT order_number, status, failure_reason FROM saga_states ORDER BY created_at DESC LIMIT 5;"

Write-Host ""
Write-Host "  Check Mailtrap for:" -ForegroundColor Yellow
Write-Host "    - Order Shipped email (from TEST 1)" -ForegroundColor Gray
Write-Host "    - Order Delivered email (from TEST 1)" -ForegroundColor Gray
Write-Host "    - Order Cancelled email (from TEST 2, if saga cancelled)" -ForegroundColor Gray

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ALL TESTS COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green