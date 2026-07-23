namespace Microsoft.eShopOnContainers.WebMVC.Controllers;

using Microsoft.eShopOnContainers.WebMVC.ViewModels;

[Authorize]
public class OrderController : Controller
{
    private readonly IOrderingService _orderSvc;
    private readonly IBasketService _basketSvc;
    private readonly IIdentityParser<ApplicationUser> _appUserParser;
    public OrderController(IOrderingService orderSvc, IBasketService basketSvc, IIdentityParser<ApplicationUser> appUserParser)
    {
        _appUserParser = appUserParser;
        _orderSvc = orderSvc;
        _basketSvc = basketSvc;
    }

    public async Task<IActionResult> Create()
    {

        var user = _appUserParser.Parse(HttpContext.User);
        var order = await _basketSvc.GetOrderDraft(user.Id);
        var vm = _orderSvc.MapUserInfoIntoOrder(user, order);
        vm.CardExpirationShortFormat();

        return View(vm);
    }

    [HttpPost]
    public async Task<IActionResult> Checkout(Order model)
    {
        try
        {
            if (ModelState.IsValid)
            {
                var basket = _orderSvc.MapOrderToBasket(model);

                await _basketSvc.Checkout(basket);

                //Redirect to historic list.
                return RedirectToAction("Index");
            }
        }
        catch (Exception ex)
        {
            ModelState.AddModelError("Error", $"It was not possible to create a new order, please try later on ({ex.GetType().Name} - {ex.Message})");
        }

        return View("Create", model);
    }

    public async Task<IActionResult> Cancel(string orderId)
    {
        try
        {
            await _orderSvc.CancelOrder(orderId);
        }
        catch (Exception ex)
        {
            ModelState.AddModelError("Error", $"It was not possible to cancel the order, please try later on ({ex.GetType().Name} - {ex.Message})");
        }

        //Redirect to historic list.
        return RedirectToAction("Index");
    }

    public async Task<IActionResult> Detail(string orderId)
    {
        var user = _appUserParser.Parse(HttpContext.User);

        var order = await _orderSvc.GetOrder(user, orderId);
        return View(order);
    }

    public async Task<IActionResult> Index(Order item) // NOSONAR S1172 S6967 - GET list action; model binding creates invalid state for complex type from empty request
    {
        var user = _appUserParser.Parse(HttpContext.User);
        var vm = await _orderSvc.GetMyOrders(user);
        return View(vm);
    }
}
