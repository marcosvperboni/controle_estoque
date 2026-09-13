import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { NotaFiscalService } from './nota-fiscal.service';
import { environment } from '../../../environments/environment';

describe('NotaFiscalService', () => {
  let service: NotaFiscalService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(NotaFiscalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve enviar o arquivo XML como multipart/form-data via POST', () => {
    const arquivo = new File(['<NFe></NFe>'], 'nfe.xml', { type: 'text/xml' });

    service.importar(arquivo).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/notas-fiscais/importar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({ fornecedor: 'Fornecedor', produtosCriados: [], produtosReabastecidos: [] });
  });
});
