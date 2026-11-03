package src;

import java.util.ArrayList;
import java.util.HashMap;

// Grafo usando lista de adjacencia
public class Grafo {

    // aresta: destino e peso
    public static class Aresta {
        int destino;
        double peso;

        Aresta(int destino, double peso) {
            this.destino = destino;
            this.peso = peso;
        }
    }

    boolean direcionado;
    boolean ponderado;

    ArrayList<ArrayList<Aresta>> adj = new ArrayList<>(); // lista de adjacencia
    ArrayList<String> nomes = new ArrayList<>();          // nome de cada vertice
    ArrayList<double[]> posicoes = new ArrayList<>();     // posicao no desenho
    HashMap<String, Integer> numeroDoNome = new HashMap<>();

    public Grafo(boolean direcionado, boolean ponderado) {
        this.direcionado = direcionado;
        this.ponderado = ponderado;
    }

    int numVertices() {
        return nomes.size();
    }

    // -1 se nao existir
    int indice(String nome) {
        Integer i = numeroDoNome.get(nome);
        if (i == null) return -1;
        return i;
    }

    // sem peso = 1
    double peso(Aresta a) {
        if (ponderado) return a.peso;
        return 1;
    }

    // adiciona vertice
    int addVertice(String nome, double x, double y) {
        int i = indice(nome);
        if (i != -1) return i;
        nomes.add(nome);
        posicoes.add(new double[]{x, y});
        adj.add(new ArrayList<>());
        numeroDoNome.put(nome, nomes.size() - 1);
        return nomes.size() - 1;
    }

    void addAresta(int u, int v, double peso) {
        if (u == v) return; // sem laco
        colocarNaLista(u, v, peso);
        if (!direcionado) {
            colocarNaLista(v, u, peso);
        }
    }

    // se ja existe, atualiza o peso
    private void colocarNaLista(int u, int v, double peso) {
        for (Aresta a : adj.get(u)) {
            if (a.destino == v) {
                a.peso = peso;
                return;
            }
        }
        adj.get(u).add(new Aresta(v, peso));
    }

    boolean temAresta(int u, int v) {
        for (Aresta a : adj.get(u)) {
            if (a.destino == v) return true;
        }
        return false;
    }

    void removerAresta(int u, int v) {
        tirarDaLista(u, v);
        if (!direcionado) {
            tirarDaLista(v, u);
        }
    }

    private void tirarDaLista(int u, int v) {
        ArrayList<Aresta> lista = adj.get(u);
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).destino == v) {
                lista.remove(i);
                return;
            }
        }
    }

    // remove vertice e ajusta os numeros
    void removerVertice(int r) {
        nomes.remove(r);
        posicoes.remove(r);
        adj.remove(r);
        for (ArrayList<Aresta> lista : adj) {
            for (int i = lista.size() - 1; i >= 0; i--) {
                Aresta a = lista.get(i);
                if (a.destino == r) {
                    lista.remove(i);
                } else if (a.destino > r) {
                    a.destino--;
                }
            }
        }
        refazerNumeros();
    }

    void renomear(int i, String novoNome) {
        nomes.set(i, novoNome);
        refazerNumeros();
    }

    private void refazerNumeros() {
        numeroDoNome.clear();
        for (int i = 0; i < nomes.size(); i++) {
            numeroDoNome.put(nomes.get(i), i);
        }
    }

    // copia trocando o tipo (direcionado ou nao)
    Grafo copiar(boolean novoDirecionado) {
        Grafo novo = new Grafo(novoDirecionado, ponderado);
        for (int i = 0; i < numVertices(); i++) {
            novo.addVertice(nomes.get(i), posicoes.get(i)[0], posicoes.get(i)[1]);
        }
        for (int u = 0; u < numVertices(); u++) {
            for (Aresta a : adj.get(u)) {
                novo.addAresta(u, a.destino, a.peso);
            }
        }
        return novo;
    }

    // evita mostrar aresta repetida
    boolean mostrar(int u, Aresta a) {
        return direcionado || u < a.destino;
    }
}