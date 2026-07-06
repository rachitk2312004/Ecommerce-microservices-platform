# Postman API Testing Guide

**Enterprise E-Commerce Order Management System**

Use this guide to demonstrate the full API flow in Postman. All requests go through the **API Gateway**.

---

## Prerequisites

1. Services running (in order):
   - Config Server → Eureka → User, Product, Inventory, Payment, Notification, Order → **API Gateway**
2. `.env` configured (MySQL + `JWT_SECRET`)
3. Postman installed

### Base URL

```
http://localhost:8080
```

### Postman Environment Variables

Create a Postman environment with:

| Variable | Initial Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | API Gateway |
| `customerToken` | *(empty)* | Set after customer login |
| `adminToken` | *(empty)* | Set after admin login |
| `customerUserId` | *(empty)* | From login/register response |
| `adminUserId` | *(empty)* | From admin login |
| `productId` | `1` | Product to order |
| `categoryId` | `1` | Category for new products |
| `orderId` | *(empty)* | Set after place order |
| `paymentId` | *(empty)* | Set after payment lookup |

### Authorization Header (protected routes)

```
Authorization: Bearer {{customerToken}}
```

For admin routes, use:

```
Authorization: Bearer {{adminToken}}
```

---

## Seeded Test Accounts (local dev)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@ecommerce.com` | `admin123` |
| Customer | `customer@ecommerce.com` | `customer123` |

---

## Important Notes

- **Cart is frontend-only** (localStorage). There is no cart API. To simulate “add to cart → checkout”, you send **product IDs + quantities** directly to `POST /api/orders`.
- **Prices are calculated on the server** — never send price in the order request.
- **Payment** is normally triggered automatically inside the order workflow. A standalone payment API is also available for testing.
- Payment methods: `CARD`, `UPI`, `NET_BANKING`, `CASH_ON_DELIVERY`

---

# FULL DEMO FLOW

## Phase 1 — Public browsing (no auth)

### 1.1 List products

```
GET {{baseUrl}}/api/products?page=0&size=10
```

**Headers:** none

---

### 1.2 Get product by ID

```
GET {{baseUrl}}/api/products/1
```

**Headers:** none

**Save:** `productId` from response for later steps.

---

### 1.3 Search products

```
GET {{baseUrl}}/api/products/search?name=headphone&page=0&size=10
```

---

### 1.4 List categories

```
GET {{baseUrl}}/api/categories
```

---

### 1.5 Products by category

```
GET {{baseUrl}}/api/products/category/1
```

---

### 1.6 Check inventory (public read via gateway still needs auth for inventory service)

> Inventory GET requires authentication. Skip until after login, or check stock after customer login:

```
GET {{baseUrl}}/api/inventory/1
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

---

## Phase 2 — Customer registration & login

### 2.1 Register new customer

```
POST {{baseUrl}}/api/auth/register
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul.sharma@example.com",
  "password": "rahul123",
  "phone": "9876543210",
  "address": "12 MG Road, Bengaluru"
}
```

**Expected:** `201 Created`

**Save from response:**
- `data.token` → `customerToken`
- `data.user.id` → `customerUserId`

**Example response shape:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 3,
      "firstName": "Rahul",
      "lastName": "Sharma",
      "email": "rahul.sharma@example.com",
      "role": "CUSTOMER",
      "active": true
    }
  }
}
```

---

### 2.2 Login existing customer

```
POST {{baseUrl}}/api/auth/login
```

**Body (raw JSON):**
```json
{
  "email": "customer@ecommerce.com",
  "password": "customer123"
}
```

**Save:** `data.token` → `customerToken`, `data.user.id` → `customerUserId`

---

### 2.3 Get customer profile

```
GET {{baseUrl}}/api/users/profile
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

---

### 2.4 Update profile

```
PUT {{baseUrl}}/api/users/profile
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

**Body:**
```json
{
  "firstName": "John",
  "lastName": "Customer",
  "phone": "8888888888",
  "address": "123 Main Street, Mumbai"
}
```

---

### 2.5 Change password

```
PUT {{baseUrl}}/api/users/change-password
```

**Body:**
```json
{
  "currentPassword": "customer123",
  "newPassword": "customer456"
}
```

---

## Phase 3 — Simulate “Add to cart” & place order

Cart has no backend API. This request is equivalent to checkout with items from the cart.

### 3.1 Place order (full workflow)

Triggers: validate products → calculate price → reserve inventory → process payment → confirm order → send notification.

```
POST {{baseUrl}}/api/orders
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 1
    },
    {
      "productId": 4,
      "quantity": 2
    }
  ],
  "paymentMethod": "UPI"
}
```

**Alternative payment methods:** `CARD`, `NET_BANKING`, `CASH_ON_DELIVERY`

**Save from response:**
- `data.id` → `orderId`
- `data.orderNumber`
- `data.orderStatus` (expect `CONFIRMED` on success)
- `data.paymentStatus` (expect `SUCCESS`)

**Example success response:**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-20260706-1234",
    "userId": 2,
    "totalAmount": 17498.00,
    "orderStatus": "CONFIRMED",
    "paymentStatus": "SUCCESS",
    "items": [
      {
        "id": 1,
        "orderId": 1,
        "productId": 1,
        "productName": "Wireless Headphones",
        "quantity": 1,
        "unitPrice": 14999.00,
        "subtotal": 14999.00
      }
    ]
  }
}
```

---

### 3.2 Get my orders

```
GET {{baseUrl}}/api/orders/my-orders
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

---

### 3.3 Get order by ID

```
GET {{baseUrl}}/api/orders/{{orderId}}
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

---

### 3.4 Cancel order

```
POST {{baseUrl}}/api/orders/{{orderId}}/cancel
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

**Body:** none

Only allowed for eligible statuses (`PENDING`, `INVENTORY_RESERVED`, `PAYMENT_PROCESSING`, `CONFIRMED`).

---

## Phase 4 — Payment service (standalone testing)

Normally payment runs inside order placement. Use these to demo payment APIs directly.

### 4.1 Process payment (simulated)

```
POST {{baseUrl}}/api/payments/process
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

**Body:**
```json
{
  "orderId": 1,
  "userId": 2,
  "amount": 14999.00,
  "paymentMethod": "CARD",
  "idempotencyKey": "PAY-TEST-001"
}
```

> Use the same `idempotencyKey` to test idempotent retry (no duplicate charge).

**Force failure (testing):** set env `PAYMENT_FORCE_FAIL=true` and restart payment-service.

---

### 4.2 Get payments by order

```
GET {{baseUrl}}/api/payments/order/{{orderId}}
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

---

### 4.3 Refund payment

```
POST {{baseUrl}}/api/payments/{{paymentId}}/refund
```

**Headers:**
```
Authorization: Bearer {{adminToken}}
```

**Body:** none

---

## Phase 5 — Notifications

### 5.1 Get customer notifications

```
GET {{baseUrl}}/api/notifications/user/{{customerUserId}}
```

**Headers:**
```
Authorization: Bearer {{customerToken}}
```

---

## Phase 6 — Admin flow

### 6.1 Admin login

```
POST {{baseUrl}}/api/auth/login
```

**Body:**
```json
{
  "email": "admin@ecommerce.com",
  "password": "admin123"
}
```

**Save:** `data.token` → `adminToken`

---

### 6.2 Create category

```
POST {{baseUrl}}/api/categories
```

**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Home & Kitchen",
  "description": "Home appliances and kitchen essentials"
}
```

---

### 6.3 List categories

```
GET {{baseUrl}}/api/categories
```

---

### 6.4 Add product (admin)

```
POST {{baseUrl}}/api/products
```

**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Bluetooth Speaker",
  "description": "Portable waterproof Bluetooth speaker with 12h battery",
  "price": 4999.00,
  "categoryId": 1,
  "imageUrl": "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=300&fit=crop&q=80"
}
```

**Save:** `data.id` as new `productId`

---

### 6.5 Update product

```
PUT {{baseUrl}}/api/products/9
```

**Body:**
```json
{
  "name": "Bluetooth Speaker Pro",
  "description": "Updated description with bass boost",
  "price": 5499.00,
  "categoryId": 1,
  "imageUrl": "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=300&fit=crop&q=80"
}
```

---

### 6.6 Deactivate product (soft delete)

```
DELETE {{baseUrl}}/api/products/9
```

**Headers:**
```
Authorization: Bearer {{adminToken}}
```

---

### 6.7 Update inventory stock (admin)

```
PUT {{baseUrl}}/api/inventory/1
```

**Headers:**
```
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

**Body:**
```json
{
  "availableQuantity": 150
}
```

---

### 6.8 Get inventory for product

```
GET {{baseUrl}}/api/inventory/1
```

**Headers:**
```
Authorization: Bearer {{adminToken}}
```

---

## Phase 7 — Inventory operations (internal-style testing)

These are used by Order Service via Feign. You can call them directly in Postman for demos.

### 7.1 Check availability

```
POST {{baseUrl}}/api/inventory/check
```

**Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

---

### 7.2 Reserve stock

```
POST {{baseUrl}}/api/inventory/reserve
```

**Body:**
```json
{
  "productId": 1,
  "quantity": 1,
  "orderId": 99
}
```

---

### 7.3 Confirm reservation

```
POST {{baseUrl}}/api/inventory/confirm
```

**Body:**
```json
{
  "productId": 1,
  "quantity": 1,
  "orderId": 99
}
```

---

### 7.4 Release reservation

```
POST {{baseUrl}}/api/inventory/release
```

**Body:**
```json
{
  "productId": 1,
  "quantity": 1,
  "orderId": 99
}
```

---

## Phase 8 — Health checks (Actuator)

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080/actuator/health |
| User Service | http://localhost:8081/actuator/health |
| Product Service | http://localhost:8082/actuator/health |
| Inventory Service | http://localhost:8083/actuator/health |
| Order Service | http://localhost:8084/actuator/health |
| Payment Service | http://localhost:8085/actuator/health |
| Notification Service | http://localhost:8086/actuator/health |
| Eureka | http://localhost:8761 |

---

## Swagger UI (per service)

| Service | URL |
|---------|-----|
| User | http://localhost:8081/swagger-ui.html |
| Product | http://localhost:8082/swagger-ui.html |
| Inventory | http://localhost:8083/swagger-ui.html |
| Order | http://localhost:8084/swagger-ui.html |
| Payment | http://localhost:8085/swagger-ui.html |
| Notification | http://localhost:8086/swagger-ui.html |

---

# Recommended Postman Demo Script (5–10 min)

| Step | Action | Endpoint |
|------|--------|----------|
| 1 | Browse products (public) | `GET /api/products` |
| 2 | Register customer | `POST /api/auth/register` |
| 3 | Login customer | `POST /api/auth/login` → save token |
| 4 | View profile | `GET /api/users/profile` |
| 5 | Check stock | `GET /api/inventory/1` |
| 6 | Place order (cart simulation) | `POST /api/orders` |
| 7 | View order history | `GET /api/orders/my-orders` |
| 8 | View notifications | `GET /api/notifications/user/{userId}` |
| 9 | Admin login | `POST /api/auth/login` (admin) |
| 10 | Add category | `POST /api/categories` |
| 11 | Add product | `POST /api/products` |
| 12 | Update inventory | `PUT /api/inventory/{productId}` |
| 13 | Cancel order (optional) | `POST /api/orders/{id}/cancel` |

---

# Common Errors

| Status | Cause | Fix |
|--------|-------|-----|
| `401` | Missing/invalid JWT | Login again, set `Authorization: Bearer <token>` |
| `403` | Customer hitting admin API | Use `adminToken` |
| `409` | Duplicate email on register | Use different email |
| `400` | Insufficient stock | Update inventory via admin |
| `502/503` | Downstream service down | Start all services + check Eureka |

---

# Postman Collection Tips

1. Create a **Collection** named `E-Commerce API`
2. Add collection variable `baseUrl` = `http://localhost:8080`
3. On login/register requests, add **Tests** tab script:
   ```javascript
   const res = pm.response.json();
   if (res.data && res.data.token) {
     pm.environment.set("customerToken", res.data.token);
     pm.environment.set("customerUserId", res.data.user.id);
   }
   ```
4. For admin login, save to `adminToken` instead
5. Duplicate the `Authorization` header on protected folders using **Bearer Token** auth type

---

# Error Response Format

```json
{
  "timestamp": "2026-07-06T08:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for product 1",
  "path": "/api/orders"
}
```

---

# Success Response Format

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { }
}
```
