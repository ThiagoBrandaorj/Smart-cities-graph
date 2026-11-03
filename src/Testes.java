package src;

import java.util.ArrayList;

// Testes (rodar com: java -cp out src.Testes)
public class Testes {

    static int falhas = 0;

    static void confere(String nome, Object obtido, Object esperado) {
        if (obtido.equals(esperado)) {
            System.out.println("OK     " + nome + " = " + obtido);
        } else {
            falhas++;
            System.out.println("FALHOU " + nome + " = " + obtido + " (esperado " + esperado + ")");
        }
    }

    static String juntar(Grafo g, int[] vertices, int quantos) {
        String s = "";
        for (int i = 0; i < quantos; i++) s += g.nomes.get(vertices[i]);
        return s;
    }

    static String juntar(Grafo g, ArrayList<Integer> vertices) {
        String s = "";
        for (int v : vertices) s += g.nomes.get(v);
        return s;
    }

    public static void main(String[] args) {
        Modelo m = new Modelo();
        m.carregarExemplo(760, 520);
        Grafo g = m.g;
        int O = g.indice("O");

        // Dijkstra a partir de O
        Algoritmos.ResultadoDijkstra dij = Algoritmos.dijkstra(g, O);
        confere("Dijkstra A", dij.dist[g.indice("A")], 2.0);
        confere("Dijkstra B", dij.dist[g.indice("B")], 4.0);
        confere("Dijkstra C", dij.dist[g.indice("C")], 4.0);
        confere("Dijkstra D", dij.dist[g.indice("D")], 8.0);
        confere("Dijkstra E", dij.dist[g.indice("E")], 7.0);
        confere("Dijkstra T", dij.dist[g.indice("T")], 13.0);
        confere("Dijkstra F", dij.dist[g.indice("F")], 14.0);
        confere("Caminho ate F", juntar(g, Algoritmos.caminho(dij.pai, g.indice("F"))), "OAF");
        confere("Caminho ate T", juntar(g, Algoritmos.caminho(dij.pai, g.indice("T"))), "OABDT");

        // Kruskal
        Algoritmos.ResultadoKruskal kru = Algoritmos.kruskal(g);
        confere("Kruskal custo", kru.custoTotal, 20.0);
        confere("Kruskal arestas", kru.escolhidas.size(), 7);

        // BFS e DFS
        Algoritmos.Busca bfs = Algoritmos.bfs(g, O);
        confere("BFS fila", juntar(g, bfs.ordem, bfs.quantos), "OABCDFET");
        confere("BFS caminho ate T", juntar(g, Algoritmos.caminho(bfs.pai, g.indice("T"))), "OADT");
        Algoritmos.Busca dfs = Algoritmos.dfs(g, O);
        confere("DFS ordem", juntar(g, dfs.ordem, dfs.quantos), "OABCEDTF");

        // grafo desconexo
        g.addVertice("Z", 0, 0);
        confere("DFS com Z isolado visita", Algoritmos.dfs(g, O).quantos, 8);
        confere("BFS com Z isolado visita", Algoritmos.bfs(g, O).quantos, 8);
        confere("Kruskal desconexo liga todos?", Algoritmos.kruskal(g).ligouTodos, false);
        confere("Dijkstra Z", Algoritmos.dijkstra(g, O).dist[g.indice("Z")], Double.POSITIVE_INFINITY);

        // peso negativo
        g.addAresta(O, g.indice("Z"), -1);
        String deu;
        try {
            Algoritmos.dijkstra(g, O);
            deu = "aceitou";
        } catch (IllegalArgumentException e) {
            deu = "recusou";
        }
        confere("Peso negativo", deu, "recusou");

        // grafo direcionado
        Grafo d = new Grafo(true, true);
        int a = d.addVertice("A", 0, 0), b = d.addVertice("B", 0, 0), c = d.addVertice("C", 0, 0);
        d.addAresta(a, b, 1);
        d.addAresta(b, c, 1);
        confere("Direcionado: BFS de C", Algoritmos.bfs(d, c).quantos, 1);
        confere("Direcionado: BFS de A", Algoritmos.bfs(d, a).quantos, 3);

        // remover vertice
        Grafo r = new Grafo(false, true);
        int x = r.addVertice("X", 0, 0), y = r.addVertice("Y", 0, 0), w = r.addVertice("W", 0, 0);
        r.addAresta(x, y, 1);
        r.addAresta(y, w, 2);
        r.removerVertice(x);
        confere("Depois de remover X, W e o vertice", r.indice("W"), 1);
        confere("Aresta Y-W continua", r.temAresta(r.indice("Y"), r.indice("W")), true);

        // DFS em grafo grande
        Grafo longo = new Grafo(false, false);
        for (int i = 0; i < 50000; i++) longo.addVertice("V" + i, 0, 0);
        for (int i = 1; i < 50000; i++) longo.addAresta(i - 1, i, 1);
        confere("DFS profunda", Algoritmos.dfs(longo, 0).quantos, 50000);

        // entrada por texto
        Modelo original = new Modelo();
        original.carregarExemplo(760, 520);
        Modelo t = new Modelo();
        t.carregarTexto(original.paraTexto(), false, true, 760, 520);
        confere("Texto -> Kruskal custo", Algoritmos.kruskal(t.g).custoTotal, 20.0);
        t.carregarTexto("# comentario\nA B 2,5\nB C\nX\n", false, true, 760, 520);
        confere("Texto com decimal e vertice isolado", Algoritmos.dijkstra(t.g, 0).dist[t.g.indice("C")], 3.5);
        try {
            t.carregarTexto("A B abc", false, true, 760, 520);
            deu = "aceitou";
        } catch (IllegalArgumentException e) {
            deu = "recusou";
        }
        confere("Texto invalido", deu, "recusou");

        // trocar para direcionado
        Grafo nd = new Grafo(false, true);
        nd.addVertice("P", 0, 0);
        nd.addVertice("Q", 0, 0);
        nd.addAresta(0, 1, 3);
        Grafo dir = nd.copiar(true);
        confere("Copia direcionada tem P->Q e Q->P", dir.temAresta(0, 1) && dir.temAresta(1, 0), true);

        System.out.println();
        System.out.println(falhas == 0 ? "Todos os testes passaram." : falhas + " teste(s) falharam.");
        System.exit(falhas == 0 ? 0 : 1);
    }
}