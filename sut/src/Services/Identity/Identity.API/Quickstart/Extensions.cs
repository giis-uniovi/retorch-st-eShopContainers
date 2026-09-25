namespace IdentityServerHost.Quickstart.UI;

public static class Extensions
{
    /// <summary>
    /// Checks if the redirect URI is for a native client.
    /// </summary>
    /// <returns></returns>
    public static bool IsNativeClient(this AuthorizationRequest context)
    {
        return !context.RedirectUri.StartsWith("https", StringComparison.Ordinal)
           && !context.RedirectUri.StartsWith("http", StringComparison.Ordinal);
    }

    public static IActionResult LoadingPage(this Controller controller, string viewName, string redirectUri)
    {
        controller.HttpContext.Response.StatusCode = 200;
        controller.HttpContext.Response.Headers.Location = "";

        return controller.View(viewName, new RedirectViewModel { RedirectUrl = redirectUri });
    }

    // Prevents open-redirect: only follows returnUrl if it's local, else goes home
    public static IActionResult RedirectToLocalOrHome(this Controller controller, string returnUrl)
    {
        if (controller.Url.IsLocalUrl(returnUrl))
        {
            return controller.Redirect(returnUrl);
        }

        return controller.Redirect("~/");
    }
}
