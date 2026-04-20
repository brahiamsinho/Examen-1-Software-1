import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { AdminApiService } from '@features/admin/admin-api.service';
import type { BitacoraDto } from '@features/admin/admin.models';

@Component({
  selector: 'app-admin-audit-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-audit-page.component.html',
  styleUrl: './admin-audit-page.component.scss',
})
export class AdminAuditPageComponent implements OnInit {
  private readonly api = inject(AdminApiService);

  rows: BitacoraDto[] = [];
  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  errorMsg = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.api.listBitacora(this.page, this.size).subscribe({
      next: (res) => {
        this.rows = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.errorMsg = this.msg(e);
      },
    });
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }

  next(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.load();
    }
  }

  private msg(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      const b = err.error as { message?: string } | null;
      if (b && typeof b.message === 'string') {
        return b.message;
      }
      return err.message;
    }
    return 'Error de red o servidor.';
  }
}
