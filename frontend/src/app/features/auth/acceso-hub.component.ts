import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-acceso-hub',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './acceso-hub.component.html',
  styleUrl: './acceso-hub.component.scss',
})
export class AccesoHubComponent {
  readonly appTitle = environment.appTitle;
}
