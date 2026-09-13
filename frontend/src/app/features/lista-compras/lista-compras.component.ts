import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize } from 'rxjs';
import { Produto } from '../../core/models/produto.model';
import { ProdutoService } from '../../core/services/produto.service';

@Component({
  selector: 'app-lista-compras',
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatListModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './lista-compras.component.html',
  styleUrl: './lista-compras.component.scss'
})
export class ListaComprasComponent implements OnInit {
  private readonly produtoService = inject(ProdutoService);

  protected readonly produtos = signal<Produto[]>([]);
  protected readonly carregando = signal(true);

  ngOnInit(): void {
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.produtoService
      .listaDeCompras()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe((produtos) => this.produtos.set(produtos));
  }

  protected sugestaoDeCompra(produto: Produto): number {
    return Math.max(produto.quantidadeMinima - produto.quantidade, produto.quantidadeMinima);
  }
}
