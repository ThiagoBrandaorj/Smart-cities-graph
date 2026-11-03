package src;

// Heap de minimo (usado no Dijkstra e no Kruskal)
public class MinHeap {

    private double[] chave = new double[16];
    private int[] valor = new int[16];
    private int tamanho = 0;

    boolean vazio() {
        return tamanho == 0;
    }

    int tamanho() {
        return tamanho;
    }

    void inserir(double c, int v) {
        if (tamanho == chave.length) {
            aumentar();
        }
        chave[tamanho] = c;
        valor[tamanho] = v;
        tamanho++;
        subir(tamanho - 1);
    }

    double menorChave() {
        return chave[0];
    }

    // remove o menor
    int removerMenor() {
        int resposta = valor[0];
        tamanho--;
        chave[0] = chave[tamanho];
        valor[0] = valor[tamanho];
        descer(0);
        return resposta;
    }

    // sobe ate a posicao certa
    private void subir(int i) {
        while (i > 0) {
            int pai = (i - 1) / 2;
            if (chave[i] >= chave[pai]) break;
            trocar(i, pai);
            i = pai;
        }
    }

    // desce ate a posicao certa
    private void descer(int i) {
        while (true) {
            int esq = 2 * i + 1;
            int dir = 2 * i + 2;
            int menor = i;
            if (esq < tamanho && chave[esq] < chave[menor]) menor = esq;
            if (dir < tamanho && chave[dir] < chave[menor]) menor = dir;
            if (menor == i) break;
            trocar(i, menor);
            i = menor;
        }
    }

    private void trocar(int a, int b) {
        double c = chave[a];
        chave[a] = chave[b];
        chave[b] = c;
        int v = valor[a];
        valor[a] = valor[b];
        valor[b] = v;
    }

    // dobra o tamanho
    private void aumentar() {
        double[] novaChave = new double[chave.length * 2];
        int[] novoValor = new int[valor.length * 2];
        for (int i = 0; i < tamanho; i++) {
            novaChave[i] = chave[i];
            novoValor[i] = valor[i];
        }
        chave = novaChave;
        valor = novoValor;
    }
}