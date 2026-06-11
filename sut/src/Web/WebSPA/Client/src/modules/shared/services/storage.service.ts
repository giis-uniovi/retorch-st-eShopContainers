import { Injectable } from '@angular/core';

@Injectable()
export class StorageService {
    private readonly storage: any;

    constructor() {
        this.storage = sessionStorage;
    }

    public retrieve(key: string): any {
        const item = this.storage.getItem(key);

        if (item && item !== 'undefined') {
            return JSON.parse(this.storage.getItem(key));
        }
    }

    public store(key: string, value: any) {
        this.storage.setItem(key, JSON.stringify(value));
    }
}
