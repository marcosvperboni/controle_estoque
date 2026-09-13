import { CurrencyPipe, DecimalPipe, PercentPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { finalize } from 'rxjs';
import { Produto } from '../../../core/models/produto.model';
import { ProdutoService } from '../../../core/services/produto.service';
import { FormularioProdutoDialogComponent } from '../formulario-produto-dialog/formulario-produto-dialog.component';

@Component({
  selector: 'app-lista-produtos',
  imports: [
    CurrencyPipe,
    DecimalPipe,
    PercentPipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './lista-produtos.component.html',
  styleUrl: './lista-produtos.component.scss'
})
export class ListaProdutosComponent implements OnInit {
  private readonly produtoService = inject(ProdutoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly produtos = signal<Produto[]>([]);
  protected readonly carregando = signal(true);

  protected readonly colunas = [
    'nome',
    'codigoBarras',
    'fornecedor',
    'marca',
    'quantidade',
    'precoVenda',
    'margem',
    'markup',
    'acoes'
  ];

  ngOnInit(): void {
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.produtoService
      .listar()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe((produtos) => this.produtos.set(produtos));
  }

  protected abrirFormulario(produto: Produto | null): void {
    const dialogRef = this.dialog.open(FormularioProdutoDialogComponent, {
      data: { produto },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.snackBar.open('Produto salvo com sucesso', 'Fechar', { duration: 3000 });
        this.carregar();
      }
    });
  }

  protected excluir(produto: Produto): void {
    if (!confirm(`Excluir o produto "${produto.nome}"? Esta acao nao pode ser desfeita.`)) {
      return;
    }

    this.produtoService.excluir(produto.id).subscribe(() => {
      this.snackBar.open('Produto excluido', 'Fechar', { duration: 3000 });
      this.carregar();
    });
  }
}
