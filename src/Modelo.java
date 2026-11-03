package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.Timer;

// Grafo atual e destaques da animacao
public class Modelo {

    Grafo g = new Grafo(false, true);

    HashMap<Integer, String> rotulos = new HashMap<>();
    HashSet<String> arestasDestacadas = new HashSet<>();

    // definidos pela Janela
    Runnable aoRepintar = () -> { };
    Runnable aoMudarGrafo = () -> { };

    private Timer animacao;

    // chave da aresta
    String chave(int u, int v) {
        if (!g.direcionado && u > v) {
            int t = u;
            u = v;
            v = t;
        }
        return u + "-" + v;
    }

    boolean destacada(int u, int v) {
        return arestasDestacadas.contains(chave(u, v));
    }

    void limparDestaque() {
        if (animacao != null) animacao.stop();
        rotulos.clear();
        arestasDestacadas.clear();
    }

    // atualiza tudo
    void grafoMudou() {
        limparDestaque();
        aoMudarGrafo.run();
        aoRepintar.run();
    }

    // animacao
    void animar(ArrayList<Algoritmos.Passo> passos) {
        limparDestaque();
        final int[] i = {0};
        animacao = new Timer(450, e -> {
            if (i[0] >= passos.size()) {
                animacao.stop();
                return;
            }
            Algoritmos.Passo p = passos.get(i[0]);
            i[0]++;
            if (p.vertice != -1) {
                rotulos.put(p.vertice, p.rotulo);
            } else {
                arestasDestacadas.add(chave(p.u, p.v));
                if (!rotulos.containsKey(p.u)) rotulos.put(p.u, "");
                if (!rotulos.containsKey(p.v)) rotulos.put(p.v, "");
            }
            aoRepintar.run();
        });
        animacao.start();
    }

    // proximo nome livre
    String proximoNome() {
        for (char c = 'A'; c <= 'Z'; c++) {
            if (g.indice("" + c) == -1) return "" + c;
        }
        int n = 27;
        while (g.indice("V" + n) != -1) n++;
        return "V" + n;
    }

    // grafo do enunciado
    void carregarExemplo(double largura, double altura) {
        g = new Grafo(false, true);
        String[] vertices = {"O 0.08 0.45", "A 0.30 0.13", "F 0.80 0.13", "B 0.36 0.50",
                             "D 0.62 0.50", "T 0.90 0.52", "C 0.20 0.88", "E 0.52 0.88"};
        for (String linha : vertices) {
            String[] p = linha.split(" ");
            g.addVertice(p[0], Double.parseDouble(p[1]) * largura, Double.parseDouble(p[2]) * altura);
        }
        String[] arestas = {"O A 2", "O B 5", "O C 4", "A B 2", "A D 7", "A F 12", "B C 1",
                            "B D 4", "B E 3", "C E 4", "D E 4", "D T 5", "E T 7", "F T 3"};
        for (String linha : arestas) {
            String[] p = linha.split(" ");
            g.addAresta(g.indice(p[0]), g.indice(p[1]), Double.parseDouble(p[2]));
        }
    }

    // le o grafo digitado (ex: A B 5)
    void carregarTexto(String texto, boolean direcionado, boolean ponderado, double largura, double altura) {
        Grafo novo = new Grafo(direcionado, ponderado);
        String[] linhas = texto.split("\n");

        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i].trim();
            if (linha.isEmpty() || linha.startsWith("#")) continue;

            String[] p = linha.split("[ \t;]+");
            int numLinha = i + 1;

            if (p.length == 1) {
                novo.addVertice(p[0], 0, 0);
            } else if (p.length == 2 || p.length == 3) {
                if (p[0].equals(p[1])) {
                    throw new IllegalArgumentException("Linha " + numLinha + ": aresta de um vértice para ele mesmo.");
                }
                double peso = 1;
                if (p.length == 3) {
                    try {
                        peso = Double.parseDouble(p[2].replace(',', '.'));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Linha " + numLinha + ": peso inválido \"" + p[2] + "\".");
                    }
                }
                int u = novo.addVertice(p[0], 0, 0);
                int v = novo.addVertice(p[1], 0, 0);
                novo.addAresta(u, v, peso);
            } else {
                throw new IllegalArgumentException("Linha " + numLinha + ": use \"origem destino peso\".");
            }
        }

        // vertices em circulo
        int n = novo.numVertices();
        double raio = Math.min(largura, altura) * 0.38;
        for (int i = 0; i < n; i++) {
            double angulo = -Math.PI / 2 + 2 * Math.PI * i / n;
            novo.posicoes.get(i)[0] = largura / 2 + raio * Math.cos(angulo);
            novo.posicoes.get(i)[1] = altura / 2 + raio * Math.sin(angulo);
        }
        g = novo;
    }

    // grafo em texto
    String paraTexto() {
        StringBuilder sb = new StringBuilder();
        boolean[] temAresta = new boolean[g.numVertices()];
        for (int u = 0; u < g.numVertices(); u++) {
            for (Grafo.Aresta a : g.adj.get(u)) {
                if (!g.mostrar(u, a)) continue;
                sb.append(g.nomes.get(u)).append(" ").append(g.nomes.get(a.destino));
                if (g.ponderado) sb.append(" ").append(Algoritmos.formatar(a.peso));
                sb.append("\n");
                temAresta[u] = true;
                temAresta[a.destino] = true;
            }
        }
        for (int u = 0; u < g.numVertices(); u++) {
            if (!temAresta[u]) sb.append(g.nomes.get(u)).append("\n");
        }
        return sb.toString();
    }
}