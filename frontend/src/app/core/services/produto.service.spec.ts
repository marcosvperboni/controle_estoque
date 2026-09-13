import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ProdutoService } from './produto.service';
import { environment } from '../../../environments/environment';
import { Produto, ProdutoRequest } from '../models/produto.model';

describe('ProdutoService', () => {
  let service: ProdutoService;
  let httpMock: HttpTestingController;

  const produtoExemplo: Produto = {
    id: 1,
    nome: 'Refrigerante 2L',
    codigoBarras: '7891000100103',
    fornecedor: 'Distribuidora ABC',
    marca: 'Marca X',
    dataValidade: null,
    precoVarejo: 9.9,
    precoAtacado: 8.9,
    precoCompra: 5,
    precoVenda: 9.9,
    margem: 0.4949,
    markup: 1.98,
    quantidade: 100,
    quantidadeMinima: 20,
    abaixoDoLimiarDeCompra: false,
    criadoEm: '2026-01-01T00:00:00Z',
    atualizadoEm: '2026-01-01T00:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProdutoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve listar produtos', () => {
    service.listar().subscribe((produtos) => expect(produtos).toEqual([produtoExemplo]));

    const req = httpMock.expectOne(`${environment.apiUrl}/produtos`);
    expect(req.request.method).toBe('GET');
    req.flush([produtoExemplo]);
  });

  it('deve buscar a lista de compras (produtos abaixo do limiar)', () => {
    service.listaDeCompras().subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/produtos/lista-compras`);
    expect(req.request.method).toBe('GET');
    req.flush([produtoExemplo]);
  });

  it('deve buscar produto por id', () => {
    service.buscarPorId(1).subscribe((produto) => expect(produto).toEqual(produtoExemplo));

    const req = httpMock.expectOne(`${environment.apiUrl}/produtos/1`);
    expect(req.request.method).toBe('GET');
    req.flush(produtoExemplo);
  });

  it('deve cadastrar um novo produto via POST', () => {
    const request: ProdutoRequest = {
      nome: 'Refrigerante 2L',
      codigoBarras: '7891000100103',
      fornecedor: 'Distribuidora ABC',
      marca: 'Marca X',
      dataValidade: null,
      precoVarejo: 9.9,
      precoAtacado: 8.9,
      precoCompra: 5,
      precoVenda: 9.9,
      quantidade: 100,
      quantidadeMinima: 20
    };

    service.cadastrar(request).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/produtos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(produtoExemplo);
  });

  it('deve atualizar um produto via PUT', () => {
    service.atualizar(1, { ...produtoExemplo }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/produtos/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(produtoExemplo);
  });

  it('deve excluir um produto via DELETE', () => {
    service.excluir(1).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/produtos/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
