import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'produtos', pathMatch: 'full' },
  {
    path: 'produtos',
    loadComponent: () =>
      import('./features/produtos/lista-produtos/lista-produtos.component').then(
        (m) => m.ListaProdutosComponent
      )
  },
  {
    path: 'lista-compras',
    loadComponent: () =>
      import('./features/lista-compras/lista-compras.component').then(
        (m) => m.ListaComprasComponent
      )
  },
  {
    path: 'importar-nfe',
    loadComponent: () =>
      import('./features/importar-nfe/importar-nfe.component').then(
        (m) => m.ImportarNfeComponent
      )
  },
  { path: '**', redirectTo: 'produtos' }
];
