namespace Microsoft.eShopOnContainers.WebMVC.Controllers;

[Authorize]
public class CartController : Controller
{
    private readonly IBasketService _basketSvc;
    private readonly IIdentityParser<ApplicationUser> _appUserParser;

    public CartController(IBasketService basketSvc, IIdentityParser<ApplicationUser> appUserParser)
    {
        _basketSvc = basketSvc;
        _appUserParser = appUserParser;
    }

    public async Task<IActionResult> Index()
    {
        try
        {
            var user = _appUserParser.Parse(HttpContext.User);
            var vm = await _basketSvc.GetBasket(user);

            return View(vm);
        }
        catch (Exception ex)
        {
            HandleException(ex);
        }

        return View();
    }


    [HttpPost]
    public async Task<IActionResult> Index(Dictionary<string, int> quantities, string action)
    {
        if (!ModelState.IsValid) return View();
        try
        {
            var user = _appUserParser.Parse(HttpContext.User);
            await _basketSvc.SetQuantities(user, quantities);
            if (action == "[ Checkout ]")
            {
                return RedirectToAction("Create", "Order");
            }
        }
        catch (Exception ex)
        {
            HandleException(ex);
        }

        return View();
    }

    private const string CatalogControllerName = "Catalog";
    private const string IndexActionName = "Index";

    public async Task<IActionResult> AddToCart(CatalogItem productDetails)
    {
        if (!ModelState.IsValid) return RedirectToAction(IndexActionName, CatalogControllerName);
        try
        {
            if (productDetails?.Id != null)
            {
                var user = _appUserParser.Parse(HttpContext.User);
                await _basketSvc.AddItemToBasket(user, productDetails.Id.Value);
            }
            return RedirectToAction(IndexActionName, CatalogControllerName);
        }
        catch (Exception ex)
        {
            // Catch error when Basket.api is in circuit-opened mode
            HandleException(ex);
        }

        return RedirectToAction(IndexActionName, CatalogControllerName, new { errorMsg = ViewBag.BasketInoperativeMsg });
    }

    private void HandleException(Exception ex)
    {
        ViewBag.BasketInoperativeMsg = $"Basket Service is inoperative {ex.GetType().Name} - {ex.Message}";
    }
}
