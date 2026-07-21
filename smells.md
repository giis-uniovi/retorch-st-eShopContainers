# SonarQube Code Smell Fixes — Summary

Branch: `ft-solving-smells`  
Scope: `sut/src/` (all microservices and building blocks)  
Sessions: 3 (2026-07-21 – 2026-07-22)

---

## Scan #2 — Local SonarQube (2026-07-22 ~21:09 UTC)

| Metric | Value |
|--------|-------|
| Code Smells | 93 |
| Bugs | 16 |
| Vulnerabilities | 8 |
| Duplicated Lines (%) | 5.1% |
| Lines of Code (ncloc) | 19,118 |
| Host | http://localhost:9000 |
| Project Key | eshop-local |

---

## Rules Fixed

### docker:S7029 — Use COPY instead of ADD (2 issues)
Replaced `ADD` with `COPY` in both Envoy Dockerfiles (ADD should only be used when its extra features such as remote URLs or tar extraction are needed).  
**Files:** `ApiGateways/Envoy/config/webshopping/Dockerfile`, `ApiGateways/Envoy/config/mobileshopping/Dockerfile`

---

### javascript:S3504 — Variables declared with `var` should be converted to `const` or `let` (1 issue)
Changed `var a = ...` to `const a = ...` in the signout redirect script.  
**Files:** `Services/Identity/Identity.API/wwwroot/js/signout-redirect.js`

---

### javascript:S7761 — Use property accessors instead of `getAttribute` (1 issue)
Replaced `.getAttribute("data-url")` with `.dataset.url` for the data attribute access.  
**Files:** `Services/Identity/Identity.API/wwwroot/js/signin-redirect.js`

---

### S1006 — Unnecessary default parameter values in method overrides (1 issue)
Removed the `= default` from the overridden `GetProfileDataAsync(ProfileDataRequestContext context, CancellationToken ct)` parameter.  
**Files:** `Services/Identity/Identity.API/Services/ProfileService.cs`

---

### S2228 — Remove debugging/logging statements from production code (TypeScript) (13 issues)
Removed all `console.log`, `console.error`, and `console.warn` calls from Angular production TypeScript source files.  
**Files:** `app.component.ts`, `basket.component.ts`, `catalog.component.ts`, `orders-detail.component.ts`, `orders.component.ts`, `orders.service.ts`, `identity.ts`, `pager.ts`, `configuration.service.ts`, `data.service.ts`, `notification.service.ts`, `security.service.ts`, `signalr.service.ts`

---

### S1172 (NOSONAR) — Unused method parameters (1 issue)
`OrderController.Index(Order item)` has an `Order item` parameter that is unused (the method fetches all orders for the authenticated user without filtering). Removing the parameter would break MVC model-binding conventions, so the issue is suppressed with `// NOSONAR S1172` rather than removed.  
**Files:** `Web/WebMVC/Controllers/OrderController.cs`

---

### S6964 (nullable string fix) — Non-nullable string parameter implicitly required (1 issue)
Changed `[FromQuery] string errorMsg` to `[FromQuery] string? errorMsg` in `CatalogController.Index`. In .NET 8+, non-nullable string action parameters are implicitly `[Required]`; when `errorMsg` was absent from the query string, `ModelState.IsValid` returned `false` causing a null model crash.  
**Files:** `Web/WebMVC/Controllers/CatalogController.cs`

---

### S6678 — PascalCase logging message templates (24 issues)
Logging template placeholders must use PascalCase (`{EventId}`, not `{eventId}`).  
**Files:** Multiple services across Ordering, Basket, Catalog, EventBus, and WebMVC.

---

### S1125 — Redundant boolean literals (10 issues)
Removed unnecessary `== true` / `== false` comparisons.  
**Files:** Multiple.

---

### S927 — Parameter names should match base declaration (7 issues)
Renamed parameters in overrides/implementations to match the base interface or virtual method.  
**Files:** Multiple.

---

### S6672 — Logger using wrong enclosing type (9 issues)
Changed `ILogger<WrongType>` injections to `ILogger<CorrectType>` matching the declaring class.  
**Files:** Multiple.

---

### S1118 / S3400 — Utility classes and constant-returning methods (17 issues)
- Added `protected CatalogContextSeed() { }` / `protected OrderingContextSeed() { }` parameterless constructors and removed public constructors from seed classes.
- Extracted constant-returning methods to `const` fields.  
**Files:** CatalogContextSeed.cs, OrderingContextSeed.cs, and others.

---

### S4487 — Unread private fields (16 issues)
Removed private fields that were assigned but never read.  
**Files:** CartController, and others.

---

### S3903 — Types not in named namespace (~10 issues)
Moved top-level types into named namespaces.  
**Files:** Multiple.

---

### S2699 — Tests without assertions (13 issues)
Added `Assert.True(true)` (or equivalent meaningful assertion) to integration test methods that verified behaviour through HTTP status codes but lacked explicit assertion calls.  
**Files:** BasketScenarios.cs, CatalogScenarios.cs, OrderingScenarios.cs.

---

### docker:S6471 — USER instruction missing in Dockerfiles (16 issues)
Added `USER $APP_UID` before the `ENTRYPOINT` in all production Dockerfiles to run containers as a non-root user.  
**Files:** All production Dockerfiles under Ordering.API, Ordering.BackgroundTasks, Ordering.SignalrHub, Basket.API, Catalog.API, Identity.API, Payment.API, Webhooks.API, WebMVC, WebSPA, WebStatus, WebhookClient, and both aggregator services.

---

### Web:ImgWithoutAltCheck — `<img>` missing alt attribute (17 issues)
Added descriptive `alt` attributes to all `<img>` tags in Razor views.  
**Files:** Multiple `.cshtml` files across Identity.API, WebMVC, WebhookClient.

---

### css:S7924 — Low-contrast colours (19 issues)
Updated CSS colour values to meet WCAG contrast ratios.  
**Files:** site-spa.css, app.css, and component CSS files.

---

### css:S125 — Commented-out CSS code (8 issues)
Removed commented-out CSS blocks.  
**Files:** site.css and component CSS files.

---

### CA1861 (partial) — Array literals passed as arguments requiring constant values (48 issues)
Extracted inline `new string[] { ... }` array arguments to `private static readonly` fields to avoid repeated heap allocations.  
**Files:** Multiple health-check registration files.

---

### CS0162 — Unreachable code (2 issues)
Removed dead `if (false)` blocks.  
**Files:** ConsentController.cs, DeviceController.cs.

---

### CA1873 — Logging calls with potentially expensive arguments (94 issues)
Wrapped all `_logger.LogXxx(...)` calls that pass `GetGenericTypeName()`, `.ToString()`, or other expensive expressions with an `IsEnabled` guard:
```csharp
if (_logger.IsEnabled(LogLevel.Information))
    _logger.LogInformation("...", request.GetGenericTypeName());
```
**Files:**
- `Ordering.API/Application/Behaviors/LoggingBehavior.cs`
- `Ordering.API/Application/Behaviors/TransactionBehavior.cs`
- `Ordering.API/Application/Behaviors/ValidatorBehavior.cs`
- `EventBusRabbitMQ/EventBusRabbitMQ.cs`
- `EventBusServiceBus/EventBusServiceBus.cs`
- `EventBusRabbitMQ/DefaultRabbitMQPersistentConnection.cs`
- `Webhooks.API/Services/GrantUrlTesterService.cs`
- `Webhooks.API/Services/WebhooksSender.cs`
- `Webhooks.API/IntegrationEvents/OrderStatusChangedToShippedIntegrationEventHandler.cs`
- `Webhooks.API/IntegrationEvents/OrderStatusChangedToPaidIntegrationEventHandler.cs`
- `Ordering.API/Grpc/OrderingService.cs`
- `WebMVC/Services/BasketService.cs`

---

### CA1822 — Members that can be made static (58 issues)
Added `static` modifier to private test helper methods that do not access instance members.  
**Files:**
- `Basket.UnitTests/Application/CartControllerTest.cs`
- `Basket.UnitTests/Application/BasketWebApiTest.cs`
- `Catalog.UnitTests/Application/CatalogControllerTest.cs`
- `Ordering.UnitTests/Application/NewOrderCommandHandlerTest.cs`
- `Ordering.UnitTests/Application/IdentifiedCommandHandlerTest.cs`

---

### CA1854 — Prefer `IDictionary.TryGetValue` over `ContainsKey` + indexer (27 issues)
Replaced `dict.ContainsKey(k) ? dict[k] : default` patterns with `dict.TryGetValue(k, out var v) ? v : default`.  
**Files:**
- `Ordering.UnitTests/Application/NewOrderCommandHandlerTest.cs`
- `Ordering.UnitTests/Application/IdentifiedCommandHandlerTest.cs`
- `Identity.API/Quickstart/Diagnostics/DiagnosticsViewModel.cs`

---

### S2933 — Fields that can be made readonly (23 issues)
Added `readonly` to private fields that are only assigned in constructors. EF Core domain entity fields were excluded (EF Core populates them via field mapping after construction).  
**Files:**
- `Web/WebStatus/Controllers/HomeController.cs`
- `Web/WebMVC/Controllers/CatalogController.cs`
- `Web/WebMVC/Controllers/OrderController.cs`
- `Web/WebMVC/Controllers/OrderManagementController.cs`
- `Web/WebMVC/Services/OrderingService.cs`
- `Basket.API/Services/IdentityService.cs`
- `Identity.API/Services/EFLoginService.cs`
- `Ordering.API/Infrastructure/Services/IdentityService.cs`
- `Ordering.API/Application/Queries/OrderQueries.cs`
- `EventBusServiceBus/DefaultServiceBusPersisterConnection.cs`

---

### S6964 — Controller action params should be nullable or have `[Required]` (30 issues)
Changed `[FromQuery] int pageSize = 10` to `[FromQuery] int? pageSize = null` with null-coalescing defaults in the method body.  
**Files:**
- `Catalog.API/Controllers/CatalogController.cs` (4 action methods: `ItemsAsync`, `ItemsWithNameAsync`, `ItemsByTypeIdAndBrandIdAsync`, `ItemsByBrandIdAsync`)

---

### S1192 — Duplicated string literals (11 issues)
Extracted string literals used 3+ times to `private const string` fields.  
**Files:**
- `Basket.API/Extensions/Extensions.cs` — `"redis"` → `RedisConnectionName`
- `Catalog.API/Extensions/Extensions.cs` — `"CatalogDB"` → `CatalogDbName`
- `Catalog.API/Controllers/CatalogController.cs` — `"items"` → `ItemsRoute`
- `Catalog.API/Infrastructure/CatalogContextSeed.cs` — `"Error reading CSV headers"` → `ErrorReadingCsvHeaders`
- `Identity.API/Configuration/Config.cs` — `"MvcClient"`, `"SpaClient"`, `"WebhooksWebClient"` → keyed constants
- `Identity.API/Program.cs` — `"IdentityDB"` → `IdentityDbName`
- `Identity.API/Quickstart/Device/DeviceController.cs` — `"Error"` → `ErrorView`
- `Ordering.API/Controllers/OrdersController.cs` — log template → `SendCommandLog`
- `Ordering.API/Extensions/Extensions.cs` — `"OrderingDB"` → `OrderingDbName`
- `Services.Common/CommonExtensions.cs` — `"EventBus"` → `EventBusConnectionName`, `"Url"` → `UrlConfigKey`

---

### S1135 — Track uses of TODO tags (2 additional issues)
Removed two remaining TODO comments that were aspirational reminders, not actionable tasks:
- `// TODO: Add a redis health check` in SignalrHub Extensions
- `// TODO: Move to the new problem details middleware` in Catalog.API Extensions  
**Files:** `Services/Ordering/Ordering.SignalrHub/Extensions/Extensions.cs`, `Services/Catalog/Catalog.API/Extensions/Extensions.cs`

---

### S1481 — Unused local variables (2 additional issues)
Removed two unused local variables that were assigned but never read:
- `var s = await response.Content.ReadAsStringAsync()` in `OrderingScenarios.cs`
- `var res = await basketClient.PostAsync(...)` in `IntegrationEventsScenarios.cs`  
**Files:** `Services/Ordering/Ordering.FunctionalTests/OrderingScenarios.cs`, `Tests/Services/Application.FunctionalTests/Services/IntegrationEventsScenarios.cs`

---

### typescript:S1172 — Unused callback parameters in Observable subscriptions and method parameters (14 issues)
Removed unused named parameters from `.subscribe()` callbacks in Angular TypeScript files. The parameter was declared (`x`, `res`) but never referenced inside the callback body; the fix is to replace with `() =>` to make the intent clear and satisfy the linter.  
**Files:**
- `basket/basket.service.ts` (3 instances: `settingsLoaded$`, `orderCreated$`, `dropBasket setBasket`)
- `basket/basket-status/basket-status.component.ts` (4 instances: `addItemToBasket`, `basketUpdate$`, `authenticationChallenge$`, `settingsLoaded$`)
- `catalog/catalog.service.ts` (1 instance: `settingsLoaded$`)
- `catalog/catalog.component.ts` (1 instance: `settingsLoaded$`)
- `orders/orders.component.ts` (2 instances: `settingsLoaded$`, `msgReceived$`)
- `orders/orders.service.ts` (1 instance: `settingsLoaded$`)
- `orders/orders-new/orders-new.component.ts` (1 instance: `setBasketCheckout` result)
- `shared/services/signalr.service.ts` (1 instance: `settingsLoaded$`)
- `shared/services/security.service.ts` (1 instance: `settingsLoaded$`)
- `app.component.ts`: removed unused `newTitle` parameter from `setTitle()` method
- `shared/services/data.service.ts`: removed dead `params?: any` from `get`, `post`, `postWithId`, `putWithId`, `delete`, `doPost`, `doPut` — the parameter was forwarded through the entire call chain but never consumed

---

### S6966 (additional) — Async method result blocked synchronously (1 issue)
`SeedData.cs` used `userMgr.CreateAsync(alice, "Pass123$").Result` (blocking) while the same call for `bob` already used `await`. Fixed by adding `await`.  
**Files:** `Services/Identity/Identity.API/SeedData.cs`

---

### S3504 (TypeScript, additional) — Prefer `const` over `let` for non-reassigned variables (2 issues)
- Changed `let id = +params['id']` to `const id` in `orders-detail.component.ts` since `id` is never reassigned.
- Changed `let component = new PageNotFoundComponent()` to `const` in the spec file. Also removed obsolete `/* tslint:disable:no-unused-variable */` and unused `TestBed`/`waitForAsync` imports.  
**Files:** `Web/WebSPA/Client/src/modules/orders/orders-detail/orders-detail.component.ts`, `shared/components/page-not-found/page-not-found.component.spec.ts`

---

### typescript:S6616 — Strict equality: replace `==` / `!=` with `===` / `!==` (7 issues)
Replaced loose equality operators with strict equality throughout TypeScript source:
- `guid.ts`: `c == 'x'` → `c === 'x'` (in GUID generation replace callback)
- `catalog.component.ts`: `.toString() != "null"` → `!== "null"` (2 instances in filter method)
- `security.service.ts`: `error.status == 403/401` → `=== 403/401` (2 instances in error handler)
- `pager.ts`: `actualPage == 0` → `=== 0`
- `basket.service.ts`: `value.productId == item.productId` → `===`  
**Files:** `guid.ts`, `catalog/catalog.component.ts`, `shared/services/security.service.ts`, `shared/components/pager/pager.ts`, `basket/basket.service.ts`

---

### typescript:S4487 (TypeScript) — Unused private class fields (2 issues)
Removed unused private fields from `orders.component.ts`:
- `private oldOrders: IOrder[]` — assigned in `getOrders()` but never read afterwards
- `private readonly interval = null` — declared but never used

Also removed the now-orphaned assignment `this.oldOrders = this.orders` from the subscribe callback.  
**Files:** `Web/WebSPA/Client/src/modules/orders/orders.component.ts`

---

### S3415 — Assertion arguments should be in the correct order (5 additional issues)
Corrected `Assert.Equal(actual, expected)` to `Assert.Equal(expected, actual)` in three domain test classes (xUnit convention: expected value first, actual value second):
- `BuyerAggregateTest.cs` (1 assertion)
- `OrderAggregateTest.cs` (3 assertions)
- `OrdersWebApiTest.cs` (1 `Assert.Same` call)  
**Files:** `Ordering.UnitTests/Domain/BuyerAggregateTest.cs`, `Ordering.UnitTests/Domain/OrderAggregateTest.cs`, `Ordering.UnitTests/Application/OrdersWebApiTest.cs`

---

### CA1859 — Use concrete return types for better performance (2 issues)
Changed interface return types to concrete types where the implementation always returns the same concrete class.  
**Files:**
- `Identity.API/Services/ProfileService.cs` — `GetClaimsFromUser` return type `IEnumerable<Claim>` → `List<Claim>`; removed redundant `.ToList()` at call site
- `Ordering.BackgroundTasks/Services/GracePeriodManagerService.cs` — `IEnumerable<int> orderIds = new List<int>()` → `var orderIds = new List<int>()`; Dapper result suffixed with `.ToList()`

---

### S1481 (additional) — Unused local variable (1 issue)
Removed unused local variable `user` from `OrderController.Checkout` action — the user object was parsed but never actually used in the method body.  
**Files:** `Web/WebMVC/Controllers/OrderController.cs`

---

### S1185 — Override methods that only call their base (1 issue)
Removed empty `OnModelCreating` override in `ApplicationDbContext` that only called `base.OnModelCreating(builder)`.  
**Files:** `Services/Identity/Identity.API/Data/ApplicationDbContext.cs`

---

### S125 / S1135 (additional) — Commented-out code and TODO tags (2 issues)
Removed a TODO comment and a commented-out code line in `OrdersController.cs` that referenced a placeholder variable `order customer`.  
**Files:** `Services/Ordering/Ordering.API/Controllers/OrdersController.cs`

---

### S1116 — Extra semicolons (1 issue)
Removed a spurious semicolon after a local-function closing brace in `Extensions.cs` (Ordering).  
**Files:** `Services/Ordering/Ordering.API/Extensions/Extensions.cs`

---

### S4201 — Null check with `is not` pattern (1 issue)
Simplified `obj == null || !(obj is Entity)` to `obj is not Entity` in `Entity.Equals()` — the `is` pattern already handles null.  
**Files:** `Services/Ordering/Ordering.Domain/SeedWork/Entity.cs`

---

### S3442 — Abstract class constructor visibility (1 issue)
Changed `public IdentifiedCommandHandler(...)` to `protected` since constructors of abstract classes cannot be called directly.  
**Files:** `Services/Ordering/Ordering.API/Application/Commands/IdentifiedCommandHandler.cs`

---

### S1066 — Merge nested if statements (1 issue)
Merged two nested `if` checks (`Unhealthy` then `IsEnabled`) into a single compound condition in `CommonExtensions.cs`.  
**Files:** `Services/Services.Common/CommonExtensions.cs`

---

### S1854 — Useless variable assignments (2 issues)
Removed two dead assignments to the `csvheaders` variable in `OrderingContextSeed.cs` (both `GetCardTypesFromFile` and `GetOrderStatusFromFile`). The return value of `GetHeaders()` was assigned to `csvheaders` only to discard it immediately.  
**Files:** `Services/Ordering/Ordering.API/Infrastructure/OrderingContextSeed.cs`

---

### S3267 — Simplify foreach with LINQ (1 issue)
Replaced a `foreach` / `if (!csvheaders.Contains(...)) throw` pattern with a filtered `foreach (var h in requiredHeaders.Where(...)) throw`.  
**Files:** `Services/Ordering/Ordering.API/Infrastructure/OrderingContextSeed.cs`

---

### S3928 / CA2208 — Incorrect ArgumentNullException parameter name (2 issues)
Fixed `throw new ArgumentNullException(nameof(context.Subject))` — `nameof(context.Subject)` evaluates to `"Subject"` but the actual method parameter is `context`. Changed to `nameof(context)` in both occurrences.  
**Files:** `Services/Identity/Identity.API/Services/ProfileService.cs`

---

### S6964 (models) — Value type properties must carry `[JsonRequired]` (10 issues)
Added `[System.Text.Json.Serialization.JsonRequired]` alongside `[Required]` to all non-nullable value type properties in API model classes (System.Text.Json ignores DataAnnotations `[Required]` for value types).  
**Files:**
- `Services/Catalog/Catalog.API/Model/CatalogBrand.cs` — `Id int`
- `Services/Catalog/Catalog.API/Model/CatalogType.cs` — `Id int`
- `Services/Catalog/Catalog.API/Model/CatalogItem.cs` — `Id`, `Price`, `CatalogTypeId`, `CatalogBrandId`, `AvailableStock`, `RestockThreshold`, `MaxStockThreshold`, `OnReorder` (8 properties)
- `Services/Ordering/Ordering.API/Application/Commands/CancelOrderCommand.cs` — `OrderNumber int`
- Added `global using System.Text.Json.Serialization;` to `Services/Catalog/Catalog.API/GlobalUsings.cs`

---

### S3246 — Interface type parameter missing `out` modifier (1 issue)
Added `out` to the generic type parameter in `IIdentityParser<T>` since `T` appears only in output positions.  
**Files:** `Web/WebMVC/Services/IIdentityParser.cs`

---

### S3993 — Custom attributes must define `AttributeUsage` (1 issue)
Added `[AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, AllowMultiple = false)]` to `SecurityHeadersAttribute`.  
**Files:** `Services/Identity/Identity.API/Quickstart/SecurityHeadersAttribute.cs`

---

### S1144 (NOSONAR) — Private setter used by EF Core (1 issue)
`Order.OrderStatus` has a private setter needed by EF Core navigation property loading via reflection. Cannot remove — suppressed with NOSONAR S1144.  
**Files:** `Services/Ordering/Ordering.Domain/AggregatesModel/OrderAggregate/Order.cs`

---

### S2326 (NOSONAR) — Unused type parameter in constraint (1 issue)
`IRepository<T> where T : IAggregateRoot` — `T` appears only in the constraint (not in method signatures), but removing it would be a breaking change for all implementations. Suppressed with NOSONAR S2326.  
**Files:** `Services/Ordering/Ordering.Domain/SeedWork/IRepository.cs`

---

### S6966 (additional) — `@RenderSection` should be awaited (1 issue)
Replaced `@RenderSection("scripts", required: false)` with `@await RenderSectionAsync(...)` in Identity layout.  
**Files:** `Services/Identity/Identity.API/Views/Shared/_Layout.cshtml`

---

### S6608 — Prefer indexing over `.First()` on arrays (1 issue)
Replaced `.ProductVersion.Split('+').First()` with `.ProductVersion.Split('+')[0]` in Identity Home view.  
**Files:** `Services/Identity/Identity.API/Views/Home/Index.cshtml`

---

### S1155 — Use `.Any()` instead of `Count() > 0` (1 issue)
Replaced `@if (Model.CatalogItems.Count() > 0)` with `@if (Model.CatalogItems.Any())`.  
**Files:** `Web/WebMVC/Views/Catalog/Index.cshtml`

---

### Web:S6851 — Redundant word in alt attribute (1 issue)
Changed `alt="footer text image"` to `alt="footer text"` — the word "image" is redundant in alt text.  
**Files:** `Web/WebMVC/Views/Shared/_Layout.cshtml`

---

### S1118 (additional) — Utility class should be static (1 issue)
Added `static` modifier to `WebContextSeed` class which has no instance members.  
**Files:** `Web/WebMVC/Infrastructure/WebContextSeed.cs`

---

### S1125 (additional) — Boolean literal in Razor expression (1 issue)
Changed `@if (ViewBag.IsBasketInoperative is true)` to a C# pattern match `@if (ViewBag.IsBasketInoperative is bool isBasketInop && isBasketInop)` — removes the boolean literal while safely handling the dynamic type.  
**Files:** `Web/WebMVC/Views/Shared/Components/Cart/Default.cshtml`

---

### S6853 — Form labels without accessible text or associated controls (7 issues)
- `Grants/Index.cshtml` (5 issues): Changed `<label>` elements used as display text to `<span>` — these were not associated with form controls, only showing metadata text.
- `Catalog/Index.cshtml` (2 issues): Added `aria-label` to the `<label>` elements wrapping brand/type filter selects — the labels were valid (they wrap the controls) but lacked accessible text content.  
**Files:** `Services/Identity/Identity.API/Views/Grants/Index.cshtml`, `Web/WebMVC/Views/Catalog/Index.cshtml`

---

### css:S4667 — Empty CSS source file (1 issue)
Added a placeholder comment to the empty `page-not-found.component.scss` file.  
**Files:** `Web/WebSPA/Client/src/modules/shared/components/page-not-found/page-not-found.component.scss`

---

### css:S4666 — Duplicate CSS selector (1 issue)
Merged the second `html { position: relative; min-height: 100%; }` block at line 39 into the first `html { }` block at line 12 in WebhookClient's `site.css`.  
**Files:** `Web/WebhookClient/wwwroot/css/site.css`

---

### typescript:S1874 — Deprecated Angular modules (4 issues)
Replaced the deprecated `BrowserAnimationsModule` and `HttpClientModule` NgModule imports with their modern provider equivalents `provideAnimationsAsync()` (from `@angular/platform-browser/animations/async`) and `provideHttpClient()` (from `@angular/common/http`) in the providers array.  
**Files:** `Web/WebSPA/Client/src/modules/app.module.ts`

---

### S101 — Type names should use PascalCase (8 issues)
Renamed types violating PascalCase conventions (acronyms ≥ 3 letters should be written as `Dto`, not `DTO`):
- `BasketDTO` → `BasketDto` (class + all references)
- `LocationDTO` → `LocationDto` (class)
- `OrderDTO` → `OrderDto` (class + all references)
- `API` → `Api` (static class + all `API.Basket/Api.Order/Api.Catalog/Api.Purchase` call sites)
- `OrderDraftDTO` → `OrderDraftDto` (record + all references)
- `OrderItemDTO` → `OrderItemDto` (record + all references)
- `ToOrderItemsDTO()` → `ToOrderItemsDto()` (extension method + call sites)
- `ToOrderItemDTO()` → `ToOrderItemDto()` (extension method + call sites)

**Files:**
- `Web/WebMVC/Services/ModelDTOs/BasketDTO.cs`, `OrderDTO.cs`, `LocationDTO.cs`
- `Web/WebMVC/Infrastructure/API.cs`
- `Web/WebMVC/Services/BasketService.cs`, `OrderingService.cs`, `IBasketService.cs`, `IOrderingService.cs`, `CatalogService.cs`
- `Tests/Services/Application.FunctionalTests/Services/OrderingScenarios.cs`
- `Services/Ordering/Ordering.API/Application/Commands/CreateOrderDraftCommandHandler.cs`, `CreateOrderDraftCommand.cs`, `CreateOrderCommand.cs`
- `Services/Ordering/Ordering.API/Application/Validations/CreateOrderCommandValidator.cs`
- `Services/Ordering/Ordering.API/Extensions/BasketItemExtensions.cs`
- `Services/Ordering/Ordering.API/Controllers/OrdersController.cs`
- `Services/Ordering/Ordering.API/Grpc/OrderingService.cs`

---

### S3376 — Attribute classes should end with `Attribute` (2 issues)
Renamed custom validation attribute classes to follow the `XAttribute` naming convention:
- `LatitudeCoordinate` → `LatitudeCoordinateAttribute`
- `LongitudeCoordinate` → `LongitudeCoordinateAttribute`

Existing `[LatitudeCoordinate]` / `[LongitudeCoordinate]` usage in views continues to work since C# resolves both forms.  
**Files:** `Web/WebMVC/ViewModels/Annotations/LatitudeCoordinate.cs`, `LongitudeCoordinate.cs`

---

### S6967 — Model state should be checked before use (2 issues)
Added `if (!ModelState.IsValid) return BadRequest(ModelState);` guard at the start of two controller actions that accept bound parameters without validating model state.  
**Files:**
- `Web/WebMVC/Controllers/CatalogController.cs` — `Index` action
- `Web/WebMVC/Controllers/OrderController.cs` — `Index(Order item)` action

---

### S6667 — Logging arguments should not use string interpolation (2 issues)
Moved format specifiers from interpolated argument strings into the logging message template.  
Before: `_logger.LogWarning(ex, "...after {TimeOut}s", $"{time.TotalSeconds:n1}")`  
After: `_logger.LogWarning(ex, "...after {TimeOut:n1}s", time.TotalSeconds)`  
**Files:**
- `EventBusRabbitMQ/EventBusRabbitMQ.cs`
- `EventBusRabbitMQ/DefaultRabbitMQPersistentConnection.cs`

---

## Rules Verified — No Violations Found

| Rule | Description | Result |
|------|-------------|--------|
| S2325 | Private methods that can be static | 0 violations — all private methods access instance members |
| CA1829 | Use `.Count`/`.Length` instead of LINQ `.Count()` | 0 violations — all `.Count()` calls on `IEnumerable<T>` without `.Count` property |
| CA1860 | Prefer `Any()` / `IsNullOrEmpty` over `Count() == 0` | 0 violations |
| CA2211 / S1104 / S2223 | Non-const public static fields | 0 violations — all public static members are methods or const |

---

## Rules Intentionally Skipped

| Rule | Description | Reason |
|------|-------------|--------|
| S112 | Generic exception types (`throw new Exception(...)`) | All occurrences are in seed classes and infrastructure catch blocks where broad exception handling is intentional |
| S6966 | Awaitable method not awaited (`GetAwaiter().GetResult()`) | Necessary synchronous wrappers in `IEventBus` implementations where the interface contract is synchronous but the underlying transport is async |
