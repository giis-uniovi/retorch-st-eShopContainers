import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { IConfiguration } from '../models/configuration.model';
import { StorageService } from './storage.service';

import { ReplaySubject } from 'rxjs';

@Injectable()
export class ConfigurationService {
    serverSettings: IConfiguration;
    // ReplaySubject(1) replays the last emission to late subscribers,
    // preventing the race condition where services instantiated after the
    // config HTTP call completes never receive settingsLoaded$.
    private readonly settingsLoadedSource = new ReplaySubject<void>(1);
    readonly settingsLoaded$ = this.settingsLoadedSource.asObservable();
    isReady: boolean = false;

    constructor(private readonly http: HttpClient, private readonly storageService: StorageService) { }

    load() {
        const baseURI = document.baseURI.endsWith('/') ? document.baseURI : `${document.baseURI}/`;
        const url = `${baseURI}Home/Configuration`;
        this.http.get(url).subscribe((response) => {
            console.log('server settings loaded');
            this.serverSettings = response as IConfiguration;
            console.log(this.serverSettings);
            this.storageService.store('identityUrl', this.serverSettings.identityUrl);
            this.storageService.store('purchaseUrl', this.serverSettings.purchaseUrl);
            this.storageService.store('signalrHubUrl', this.serverSettings.signalrHubUrl);
            this.storageService.store('activateCampaignDetailFunction', this.serverSettings.activateCampaignDetailFunction);
            this.isReady = true;
            this.settingsLoadedSource.next();
        });
    }
}
