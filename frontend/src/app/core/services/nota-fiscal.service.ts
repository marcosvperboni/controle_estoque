import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotaFiscalImportResponse } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class NotaFiscalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notas-fiscais`;

  importar(arquivo: File): Observable<NotaFiscalImportResponse> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<NotaFiscalImportResponse>(`${this.baseUrl}/importar`, formData);
  }
}
