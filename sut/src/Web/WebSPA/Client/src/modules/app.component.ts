import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';

import { SecurityService } from './shared/services/security.service';
import { ConfigurationService } from './shared/services/configuration.service';
import { SignalrService } from './shared/services/signalr.service';
import { ToastrService } from 'ngx-toastr';
import { SharedModule } from './shared/shared.module';
import { BasketModule } from './basket/basket.module';

@Component({
  standalone: true,
  imports: [SharedModule, BasketModule],
    selector: 'esh-app',
    styleUrls: ['./app.component.scss'],
    templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
    Authenticated: boolean = false;
    subscription: Subscription;

    constructor(
        private readonly titleService: Title,
        public router: Router,
        private readonly securityService: SecurityService,
        private readonly configurationService: ConfigurationService,
        private readonly signalrService: SignalrService,
        private readonly toastr: ToastrService
    ) {
        this.Authenticated = this.securityService.IsAuthorized;
    }

    ngOnInit() {
        console.log('app on init');
        this.subscription = this.securityService.authenticationChallenge$.subscribe(res => this.Authenticated = res);

        console.log('configuration');
        this.configurationService.load();
    }

    public setTitle(newTitle: string) {
        this.titleService.setTitle('eShopOnContainers');
    }
}
