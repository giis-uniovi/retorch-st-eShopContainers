import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';

import { SecurityService } from '../../services/security.service';
import { SignalrService } from '../../services/signalr.service';

@Component({
  standalone: false,
    selector: 'esh-identity',
    templateUrl: './identity.html',
    styleUrls: ['./identity.scss']
})
export class Identity implements OnInit {
    authenticated: boolean = false;
    private subscription: Subscription;
    private userName: string = '';

    constructor(private readonly service: SecurityService, private readonly signalrService: SignalrService) { }

    ngOnInit() {
        this.subscription = this.service.authenticationChallenge$.subscribe(res => {
            this.authenticated = res;
            this.userName = this.service.UserData.email;
        });

        if (globalThis.location.hash) {
            this.service.AuthorizedCallback();
        }

        this.authenticated = this.service.IsAuthorized;

        if (this.authenticated) {
            if (this.service.UserData)
                this.userName = this.service.UserData.email;
        }
    }

    logoutClicked(event: any) {
        event.preventDefault();
        this.logout();
    }

    login() {
        this.service.Authorize();
    }

    logout() {
        this.signalrService.stop();
        this.service.Logoff();
    }
}
