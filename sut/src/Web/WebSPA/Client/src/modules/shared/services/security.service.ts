import { Injectable } from '@angular/core';

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Router, ActivatedRoute } from '@angular/router';
import { ConfigurationService } from './configuration.service';
import { StorageService } from './storage.service';

@Injectable()
export class SecurityService {

    private readonly storage: StorageService;
    private readonly authenticationSource = new Subject<boolean>();
    readonly authenticationChallenge$ = this.authenticationSource.asObservable();
    private authorityUrl = '';

    constructor(
        private readonly _http: HttpClient,
        private readonly _router: Router,
        private readonly route: ActivatedRoute,
        private readonly _configurationService: ConfigurationService,
        private readonly _storageService: StorageService
    ) {
        this.storage = _storageService;

        this._configurationService.settingsLoaded$.subscribe(() => {
            this.authorityUrl = this._configurationService.serverSettings.identityUrl;
            this.storage.store('IdentityUrl', this.authorityUrl);
        });

        if (this.storage.retrieve('IsAuthorized') !== '') {
            this.IsAuthorized = this.storage.retrieve('IsAuthorized');
            this.authenticationSource.next(true);
            this.UserData = this.storage.retrieve('userData');
        }
    }

    public IsAuthorized: boolean;

    public GetToken(): any {
        return this.storage.retrieve('authorizationData');
    }

    public ResetAuthorizationData() {
        this.storage.store('authorizationData', '');
        this.storage.store('authorizationDataIdToken', '');

        this.IsAuthorized = false;
        this.storage.store('IsAuthorized', false);
    }

    public UserData: any;

    public SetAuthorizationData(token: any, id_token: any) {
        if (this.storage.retrieve('authorizationData') !== '') {
            this.storage.store('authorizationData', '');
        }

        this.storage.store('authorizationData', token);
        this.storage.store('authorizationDataIdToken', id_token);
        this.IsAuthorized = true;
        this.storage.store('IsAuthorized', true);

        this.getUserData()
            .subscribe({
                next: data => {
                    this.UserData = data;
                    this.storage.store('userData', data);
                    this.authenticationSource.next(true);
                    globalThis.location.href = location.origin;
                },
                error: error => this.HandleError(error),
                complete: () => { }
            });
    }

    public Authorize() {
        this.ResetAuthorizationData();

        const authorizationUrl = this.authorityUrl + '/connect/authorize';
        const client_id = 'js';
        const redirect_uri = location.origin + '/';
        const response_type = 'id_token token';
        const scope = 'openid profile orders basket webshoppingagg orders.signalrhub';
        const nonce = 'N' + Math.random() + '' + Date.now();
        const state = Date.now() + '' + Math.random();

        this.storage.store('authStateControl', state);
        this.storage.store('authNonce', nonce);

        const url =
            authorizationUrl + '?' +
            'response_type=' + encodeURI(response_type) + '&' +
            'client_id=' + encodeURI(client_id) + '&' +
            'redirect_uri=' + encodeURI(redirect_uri) + '&' +
            'scope=' + encodeURI(scope) + '&' +
            'nonce=' + encodeURI(nonce) + '&' +
            'state=' + encodeURI(state);

        globalThis.location.href = url;
    }

    public AuthorizedCallback() {
        this.ResetAuthorizationData();

        const hash = globalThis.location.hash.slice(1);

        const result: any = hash.split('&').reduce(function (result: any, item: string) {
            const parts = item.split('=');
            result[parts[0]] = parts[1];
            return result;
        }, {});

        let token = '';
        let id_token = '';
        let authResponseIsValid = false;

        if (result.error) {
            return;
        }

        if (result.state === this.storage.retrieve('authStateControl')) {
            token = result.access_token;
            id_token = result.id_token;

            const dataIdToken: any = this.getDataFromToken(id_token);

            if (dataIdToken.nonce === this.storage.retrieve('authNonce')) {
                this.storage.store('authNonce', '');
                this.storage.store('authStateControl', '');

                authResponseIsValid = true;
            }
        }

        if (authResponseIsValid) {
            this.SetAuthorizationData(token, id_token);
        }
    }

    public Logoff() {
        const authorizationUrl = this.authorityUrl + '/connect/endsession';
        const id_token_hint = this.storage.retrieve('authorizationDataIdToken');
        const post_logout_redirect_uri = location.origin + '/';

        const url =
            authorizationUrl + '?' +
            'id_token_hint=' + encodeURI(id_token_hint) + '&' +
            'post_logout_redirect_uri=' + encodeURI(post_logout_redirect_uri);

        this.ResetAuthorizationData();

        this.authenticationSource.next(false);
        globalThis.location.href = url;
    }

    public HandleError(error: any) {
        if (error.status === 403) {
            this._router.navigate(['/Forbidden']);
        } else if (error.status === 401) {
            this._router.navigate(['/Unauthorized']);
        }
    }

    private urlBase64Decode(str: string) {
        let output = str.replace('-', '+').replace('_', '/');
        switch (output.length % 4) {
            case 0:
                break;
            case 2:
                output += '==';
                break;
            case 3:
                output += '=';
                break;
            default:
                throw new Error('Illegal base64url string!');
        }

        return globalThis.atob(output);
    }

    private getDataFromToken(token: any) {
        let data = {};

        if (token !== undefined) {
            const encoded = token.split('.')[1];
            data = JSON.parse(this.urlBase64Decode(encoded));
        }

        return data;
    }

    private readonly getUserData = (): Observable<string[]> => {
        if (this.authorityUrl === '') {
            this.authorityUrl = this.storage.retrieve('IdentityUrl');
        }

        const options = this.setHeaders();

        return this._http.get<string[]>(`${this.authorityUrl}/connect/userinfo`, options) as unknown as Observable<string[]>;
    }

    private setHeaders(): any {
        const httpOptions = {
            headers: new HttpHeaders()
        };

        httpOptions.headers = httpOptions.headers.set('Content-Type', 'application/json');
        httpOptions.headers = httpOptions.headers.set('Accept', 'application/json');

        const token = this.GetToken();

        if (token !== '') {
            httpOptions.headers = httpOptions.headers.set('Authorization', `Bearer ${token}`);
        }

        return httpOptions;
    }
}
