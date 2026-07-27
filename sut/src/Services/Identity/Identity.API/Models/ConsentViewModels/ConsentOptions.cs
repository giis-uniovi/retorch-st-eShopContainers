namespace Microsoft.eShopOnContainers.Services.Identity.API.Models.ConsentViewModels
{
    public static class ConsentOptions
    {
        public const bool EnableOfflineAccess = true;
        public const string OfflineAccessDisplayName = "Offline Access";
        public const string OfflineAccessDescription = "Access to your applications and resources, even when you are offline";

        public const string MustChooseOneErrorMessage = "You must pick at least one permission";
        public const string InvalidSelectionErrorMessage = "Invalid selection";
    }
}