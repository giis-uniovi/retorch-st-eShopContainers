import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";

import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { SecurityService } from './security.service';
import { Guid } from '../../../guid';

@Injectable()
export class DataService {
    constructor(private readonly http: HttpClient, private readonly securityService: SecurityService) { }

    get(url: string): Observable<Response> {
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

    postWithId(url: string, data: any): Observable<Response> {
        return this.doPost(url, data, true);
    }

    post(url: string, data: any): Observable<Response> {
        return this.doPost(url, data, false);
    }

    putWithId(url: string, data: any): Observable<Response> {
        return this.doPut(url, data, true);
    }

    private doPost(url: string, data: any, needId: boolean): Observable<Response> {
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

    delete(url: string) {
        const options = { };
        this.setHeaders(options);

        this.http.delete(url, options).subscribe();
    }

    private handleError(error: any) {
        return throwError(() => new Error(String(error || 'server error')));
    }

    private doPut(url: string, data: any, needId: boolean): Observable<Response> {
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
