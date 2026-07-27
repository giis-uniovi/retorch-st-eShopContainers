// Copyright (c) Brock Allen & Dominick Baier. All rights reserved.
// Licensed under the Apache License, Version 2.0. See LICENSE in the project root for license information.

namespace IdentityServerHost.Quickstart.UI;

public static class ScopeViewModelHelper
{
    public static ScopeViewModel CreateScopeViewModel(IdentityResource identity, bool check)
    {
        return new ScopeViewModel
        {
            Value = identity.Name,
            DisplayName = identity.DisplayName ?? identity.Name,
            Description = identity.Description,
            Emphasize = identity.Emphasize,
            Required = identity.Required,
            Checked = check || identity.Required
        };
    }

    public static ScopeViewModel CreateScopeViewModel(ParsedScopeValue parsedScopeValue, ApiScope apiScope, bool check)
    {
        var displayName = apiScope.DisplayName ?? apiScope.Name;
        if (!string.IsNullOrWhiteSpace(parsedScopeValue.ParsedParameter))
        {
            displayName += ":" + parsedScopeValue.ParsedParameter;
        }

        return new ScopeViewModel
        {
            Value = parsedScopeValue.RawValue,
            DisplayName = displayName,
            Description = apiScope.Description,
            Emphasize = apiScope.Emphasize,
            Required = apiScope.Required,
            Checked = check || apiScope.Required
        };
    }

    public static ScopeViewModel GetOfflineAccessScope(bool check)
    {
        return new ScopeViewModel
        {
            Value = IdentityServerConstants.StandardScopes.OfflineAccess,
            DisplayName = ConsentOptions.OfflineAccessDisplayName,
            Description = ConsentOptions.OfflineAccessDescription,
            Emphasize = true,
            Checked = check
        };
    }

    public static void PopulateConsentViewModel(ConsentViewModel vm, bool isModelNull, Client client, ResourceValidationResult validatedResources)
    {
        vm.ClientName = client.ClientName ?? client.ClientId;
        vm.ClientUrl = client.ClientUri;
        vm.ClientLogoUrl = client.LogoUri;
        vm.AllowRememberConsent = client.AllowRememberConsent;

        vm.IdentityScopes = validatedResources.Resources.IdentityResources
            .Select(x => CreateScopeViewModel(x, vm.ScopesConsented.Contains(x.Name) || isModelNull))
            .ToArray();

        var apiScopes = new List<ScopeViewModel>();
        foreach (var parsedScope in validatedResources.ParsedScopes)
        {
            var apiScope = validatedResources.Resources.FindApiScope(parsedScope.ParsedName);
            if (apiScope != null)
                apiScopes.Add(CreateScopeViewModel(parsedScope, apiScope, vm.ScopesConsented.Contains(parsedScope.RawValue) || isModelNull));
        }
        if (ConsentOptions.EnableOfflineAccess && validatedResources.Resources.OfflineAccess)
            apiScopes.Add(GetOfflineAccessScope(vm.ScopesConsented.Contains(IdentityServerConstants.StandardScopes.OfflineAccess) || isModelNull));
        vm.ApiScopes = apiScopes;
    }

    public static async Task<ConsentResponse> BuildConsentResponseAsync(
        ConsentInputModel model, ProcessConsentResult result, IEventService events,
        string subjectId, string clientId, IEnumerable<string> rawScopeValues, CancellationToken ct)
    {
        if (model.Button != "yes" || model.ScopesConsented == null || !model.ScopesConsented.Any())
        {
            result.ValidationError = model.Button == "yes"
                ? ConsentOptions.MustChooseOneErrorMessage
                : ConsentOptions.InvalidSelectionErrorMessage;
            return null;
        }
        var grantedConsent = new ConsentResponse
        {
            RememberConsent = model.RememberConsent ?? false,
            ScopesValuesConsented = model.ScopesConsented.ToArray(),
            Description = model.Description
        };
        await events.RaiseAsync(new ConsentGrantedEvent(subjectId, clientId, rawScopeValues, grantedConsent.ScopesValuesConsented, grantedConsent.RememberConsent), ct);
        return grantedConsent;
    }
}
