import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";

import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { SecurityService } from './security.service';
import { Guid } from '../../../guid';

@Injectable()
export class DataService {
    constructor(private readonly http: HttpClient, private readonly securityService: SecurityService) { }

    get(url: string, params?: any): Observable<Response> {
        const options = { };
        this.setHeaders(options);

        return this.http.get(url, options)
            .pipe(
                tap((res: Response) => {
                    return res;
                }),
                catchError(this.handleError)
            );
    }

    postWithId(url: string, data: any, params?: any): Observable<Response> {
        return this.doPost(url, data, true, params);
    }

    post(url: string, data: any, params?: any): Observable<Response> {
        return this.doPost(url, data, false, params);
    }

    putWithId(url: string, data: any, params?: any): Observable<Response> {
        return this.doPut(url, data, true, params);
    }

    private doPost(url: string, data: any, needId: boolean, params?: any): Observable<Response> {
        const options = { };
        this.setHeaders(options, needId);

        return this.http.post(url, data, options)
            .pipe(
                tap((res: Response) => {
                    return res;
                }),
                catchError(this.handleError)
            );
    }

    delete(url: string, params?: any) {
        const options = { };
        this.setHeaders(options);

        console.log('data.service deleting');

        this.http.delete(url, options)
            .subscribe((res) => { console.log('deleted'); });
    }

    private handleError(error: any) {
        if (error.error instanceof ErrorEvent) {
            console.error('Client side network error occurred:', error.error.message);
        } else {
            console.error('Backend - ' +
                `status: ${error.status}, ` +
                `statusText: ${error.statusText}, ` +
                `message: ${error.error.message}`);
        }

        return throwError(() => new Error(String(error || 'server error')));
    }

    private doPut(url: string, data: any, needId: boolean, params?: any): Observable<Response> {
        const options = { };
        this.setHeaders(options, needId);

        return this.http.put(url, data, options)
            .pipe(
                tap((res: Response) => {
                    return res;
                }),
                catchError(this.handleError)
            );
    }

    private setHeaders(options: any, needId?: boolean) {
        if (needId && this.securityService) {
            options["headers"] = new HttpHeaders()
                .append('authorization', 'Bearer ' + this.securityService.GetToken())
                .append('x-requestid', Guid.newGuid());
        } else if (this.securityService) {
            options["headers"] = new HttpHeaders()
                .append('authorization', 'Bearer ' + this.securityService.GetToken());
        }
    }
}
