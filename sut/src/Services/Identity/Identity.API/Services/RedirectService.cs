namespace Microsoft.eShopOnContainers.Services.Identity.API.Services
{
    public class RedirectService : IRedirectService
    {
        public string ExtractRedirectUriFromReturnUrl(string url)
        {
            var decodedUrl = System.Net.WebUtility.HtmlDecode(url);
            var results = decodedUrl.Split("redirect_uri=");
            if (results.Length < 2)
                return "";

            string result = results[1];

            string splitKey = result.Contains("signin-oidc") ? "signin-oidc" : "scope";

            results = result.Split(splitKey);
            if (results.Length < 2)
                return "";

            result = results[0];

            return result.Replace("%3A", ":").Replace("%2F", "/").Replace("&", "");
        }
    }
}
