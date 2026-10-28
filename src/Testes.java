package src;
/** Testes simples dos algoritmos no grafo do enunciado. Rode: java -cp out Testes */
public class Testes {
    static int falhas = 0;
    static void confere(String nome, Object obtido, Object esperado) {
        boolean ok = obtido.equals(esperado);
        if (!ok) falhas++;
        System.out.println((ok ? "OK    " : "FALHA ") + nome + " -> " + obtido + (ok ? "" : " (esperado " + esperado + ")"));
    }
    public static void main(String[] args) {
        Modelo m = new Modelo(); m.carregarExemplo(760, 520); Grafo g = m.g;
        var dij = Algoritmos.dijkstra(g, "O");
        confere("Dijkstra A", dij.dist().get("A"), 2.0); confere("Dijkstra B", dij.dist().get("B"), 4.0);
        confere("Dijkstra C", dij.dist().get("C"), 4.0); confere("Dijkstra D", dij.dist().get("D"), 8.0);
        confere("Dijkstra E", dij.dist().get("E"), 7.0); confere("Dijkstra T", dij.dist().get("T"), 13.0);
        confere("Dijkstra F", dij.dist().get("F"), 14.0); confere("Caminho F", dij.caminho("F"), java.util.List.of("O", "A", "F"));
        var kru = Algoritmos.kruskal(g);
        confere("Kruskal custo", kru.total(), 20.0); confere("Kruskal arestas", kru.escolhidas().size(), 7);
        confere("BFS fila", String.join("", Algoritmos.bfs(g, "O").fila()), "OABCDFET");
        confere("DFS ordem", String.join("", Algoritmos.dfs(g, "O").ordem()), "OABCEDTF");
        g.addVertice("Z");
        confere("DFS não alcançados", Algoritmos.dfs(g, "O").naoAlcancados(), java.util.List.of("Z"));
        confere("Kruskal desconexo", Algoritmos.kruskal(g).geradora(), false);
        g.addAresta("O", "Z", -1);
        try { Algoritmos.dijkstra(g, "O"); confere("Peso negativo", "aceitou", "erro"); } catch (IllegalArgumentException e) { confere("Peso negativo", "erro", "erro"); }
        System.exit(falhas == 0 ? 0 : 1);
    }
}
