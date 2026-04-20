import 'package:flutter_test/flutter_test.dart';
import 'package:tramites_cliente/main.dart';

void main() {
  testWidgets('App arranca', (WidgetTester tester) async {
    await tester.pumpWidget(const TramitesClienteApp());
    await tester.pumpAndSettle();
    expect(find.textContaining('Seguimiento'), findsOneWidget);
  });
}
