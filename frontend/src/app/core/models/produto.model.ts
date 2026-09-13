export interface Produto {
  id: number;
  nome: string;
  codigoBarras: string;
  fornecedor: string;
  marca: string;
  dataValidade: string | null;
  precoVarejo: number;
  precoAtacado: number;
  precoCompra: number;
  precoVenda: number;
  margem: number;
  markup: number;
  quantidade: number;
  quantidadeMinima: number;
  abaixoDoLimiarDeCompra: boolean;
  criadoEm: string;
  atualizadoEm: string;
}

export type ProdutoRequest = Omit<
  Produto,
  'id' | 'margem' | 'markup' | 'abaixoDoLimiarDeCompra' | 'criadoEm' | 'atualizadoEm'
>;

export interface NotaFiscalImportResponse {
  fornecedor: string;
  produtosCriados: Produto[];
  produtosReabastecidos: Produto[];
}
