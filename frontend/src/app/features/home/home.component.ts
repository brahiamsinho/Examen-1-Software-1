import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="home-container">
      <div class="status-card">
        <div class="status-badge up">● Sistema activo</div>
        <h1>Plataforma de Gestión de Trámites</h1>
        <p class="subtitle">Base del proyecto inicializada correctamente.</p>
        <p class="subtitle">Frontend Angular ✓ — Backend SpringBoot ✓ — FastAPI ✓</p>

        <div class="stack-grid">
          <div class="stack-item">
            <span class="stack-icon">🅰️</span>
            <span>Angular 17</span>
          </div>
          <div class="stack-item">
            <span class="stack-icon">🍃</span>
            <span>Spring Boot</span>
          </div>
          <div class="stack-item">
            <span class="stack-icon">⚡</span>
            <span>FastAPI</span>
          </div>
          <div class="stack-item">
            <span class="stack-icon">🍃</span>
            <span>MongoDB</span>
          </div>
          <div class="stack-item">
            <span class="stack-icon">🔴</span>
            <span>Redis</span>
          </div>
          <div class="stack-item">
            <span class="stack-icon">🔄</span>
            <span>Nginx</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .home-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      padding: 2rem;
    }

    .status-card {
      background: #fff;
      border-radius: 16px;
      padding: 3rem;
      max-width: 600px;
      width: 100%;
      box-shadow: 0 8px 32px rgba(0,0,0,0.08);
      text-align: center;
    }

    .status-badge {
      display: inline-block;
      padding: 0.3rem 1rem;
      border-radius: 20px;
      font-size: 0.85rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
    }

    .status-badge.up {
      background: #e6f4ea;
      color: #34a853;
    }

    h1 {
      font-size: 1.6rem;
      font-weight: 700;
      color: #202124;
      margin-bottom: 0.75rem;
    }

    .subtitle {
      color: #5f6368;
      font-size: 0.95rem;
      margin-bottom: 0.5rem;
    }

    .stack-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1rem;
      margin-top: 2rem;
    }

    .stack-item {
      background: #f8f9fa;
      border-radius: 10px;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.85rem;
      font-weight: 500;
      color: #3c4043;
    }

    .stack-icon {
      font-size: 1.4rem;
    }
  `]
})
export class HomeComponent {}
