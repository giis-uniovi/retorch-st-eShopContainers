// Copyright (c) Brock Allen & Dominick Baier. All rights reserved.
// Licensed under the Apache License, Version 2.0. See LICENSE in the project root for license information.

namespace IdentityServerHost.Quickstart.UI;

/// <summary>
/// This controller processes the consent UI
/// </summary>
[SecurityHeaders]
[Authorize]
public class ConsentController : Controller
{
    private readonly IIdentityServerInteractionService _interaction;
    private readonly IEventService _events;
    private readonly ILogger<ConsentController> _logger;

    public ConsentController(
        IIdentityServerInteractionService interaction,
        IEventService events,
        ILogger<ConsentController> logger)
    {
        _interaction = interaction;
        _events = events;
        _logger = logger;
    }

    /// <summary>
    /// Shows the consent screen
    /// </summary>
    /// <param name="returnUrl"></param>
    /// <returns></returns>
    [HttpGet]
    public async Task<IActionResult> Index(string returnUrl)
    {
        var vm = await BuildViewModelAsync(returnUrl);
        if (vm != null)
        {
            return View("Index", vm);
        }

        return View("Error");
    }

    /// <summary>
    /// Handles the consent screen postback
    /// </summary>
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Index(ConsentInputModel model)
    {
        var result = await ProcessConsent(model);

        if (result.IsRedirect)
        {
            var context = await _interaction.GetAuthorizationContextAsync(model.ReturnUrl, HttpContext.RequestAborted);
            if (context?.IsNativeClient() == true)
            {
                // The client is native, so this change in how to
                // return the response is for better UX for the end user.
                return this.LoadingPage("Redirect", result.RedirectUri);
            }

            return Redirect(result.RedirectUri);
        }

        if (result.HasValidationError)
        {
            ModelState.AddModelError(string.Empty, result.ValidationError);
        }

        if (result.ShowView)
        {
            return View("Index", result.ViewModel);
        }

        return View("Error");
    }

    // helper APIs for the ConsentController
    private async Task<ProcessConsentResult> ProcessConsent(ConsentInputModel model)
    {
        var result = new ProcessConsentResult();

        // validate return url is still valid
        var request = await _interaction.GetAuthorizationContextAsync(model.ReturnUrl, HttpContext.RequestAborted);
        if (request == null) return result;

        ConsentResponse grantedConsent = null;

        // user clicked 'no' - send back the standard 'access_denied' response
        if (model?.Button == "no")
        {
            grantedConsent = new ConsentResponse { Error = InteractionError.AccessDenied };
            await _events.RaiseAsync(new ConsentDeniedEvent(User.GetSubjectId(), request.Client.ClientId, request.ValidatedResources.RawScopeValues), HttpContext.RequestAborted);
        }
        else
        {
            grantedConsent = await ScopeViewModelHelper.BuildConsentResponseAsync(
                model, result, _events, User.GetSubjectId(), request.Client.ClientId,
                request.ValidatedResources.RawScopeValues, HttpContext.RequestAborted);
        }

        if (grantedConsent != null)
        {
            // communicate outcome of consent back to identityserver
            await _interaction.GrantConsentAsync(request, grantedConsent, HttpContext.RequestAborted);

            // indicate that's it ok to redirect back to authorization endpoint
            result.RedirectUri = model.ReturnUrl;
            result.Client = request.Client;
        }
        else
        {
            // we need to redisplay the consent UI
            result.ViewModel = await BuildViewModelAsync(model.ReturnUrl, model);
        }

        return result;
    }

    private async Task<ConsentViewModel> BuildViewModelAsync(string returnUrl, ConsentInputModel model = null)
    {
        var request = await _interaction.GetAuthorizationContextAsync(returnUrl, HttpContext.RequestAborted);
        if (request != null)
        {
            return CreateConsentViewModel(model, returnUrl, request);
        }
        else
        {
            _logger.LogError("No consent request matching request: {ReturnUrl}", returnUrl);
        }

        return null;
    }

    private static ConsentViewModel CreateConsentViewModel(
        ConsentInputModel model, string returnUrl,
        AuthorizationRequest request)
    {
        var vm = new ConsentViewModel
        {
            RememberConsent = model?.RememberConsent ?? true,
            ScopesConsented = model?.ScopesConsented ?? Enumerable.Empty<string>(),
            Description = model?.Description,
            ReturnUrl = returnUrl,
        };
        ScopeViewModelHelper.PopulateConsentViewModel(vm, model == null, request.Client, request.ValidatedResources);
        return vm;
    }

}
