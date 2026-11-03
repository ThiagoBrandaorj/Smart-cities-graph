## Sobre
Aplicação desktop em **Java (Swing)** que representa uma cidade como grafo (vértices = bairros,
arestas = vias ou cabos com custo) e executa **DFS, BFS, Dijkstra e Kruskal**.

Nesta versão, as estruturas usadas pelos algoritmos (**pilha, fila, heap e Union-Find**) foram
implementadas pelo grupo, sem as classes prontas do Java (`Stack`, `Queue`, `PriorityQueue`,
`Collections.sort`). Do Java usamos apenas `ArrayList`, `HashMap` (para achar um vértice pelo nome)
e o Swing para a interface.

## Como executar
Requisito: **JDK 17 ou superior** (confira com `javac -version`).

**Windows:** dois cliques em `executar.bat`, ou no PowerShell, na raiz do projeto:
```powershell
javac -encoding UTF-8 -d out (Get-ChildItem src\*.java).FullName
java -cp out src.Main
```

**Linux/macOS:** `./executar.sh`, ou:
```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out src.Main
```

**Testes:** `java -cp out src.Testes`

## Como usar
- **Inserir por texto:** uma aresta por linha, `origem destino peso` (ex.: `A B 5`). Sem peso vale 1;
  um nome sozinho cria um vértice isolado. Marque **Direcionado** / **Ponderado** na própria janela.
- **Desenhando:** modo **Vértice** (clique cria, duplo clique renomeia), **Aresta** (clique na origem
  e no destino), **Mover** e **Apagar**.
- **Carregar grafo do enunciado:** insere o grafo de teste do PDF.
- Escolha a **Origem** e clique em **DFS**, **BFS**, **Dijkstra** ou **Kruskal**. O resultado aparece
  em tabela e é animado no desenho (2D ou 3D).
- **Apagar grafo** limpa tudo; **Finalizar aplicação** encerra o programa.

## Estrutura de dados: lista de adjacência
Cada vértice recebe um número de 0 a V-1. `adj.get(u)` é a lista das arestas que saem de `u`
(destino e peso). Em grafo não-direcionado, a aresta A-B fica nas duas listas.

A área **"Memória: lista de adjacência"** da tela mostra exatamente essa estrutura.

**Por que lista e não matriz?** Uma cidade é um grafo esparso: cada bairro liga a poucos vizinhos.

| | Lista | Matriz |
|---|---|---|
| Memória | O(V + E) | O(V²) |
| Percorrer os vizinhos de um vértice | O(grau) | O(V) |
| DFS / BFS completas | O(V + E) | O(V²) |

## Como os algoritmos funcionam (sem bibliotecas)

### DFS — busca em profundidade — O(V + E)
Vai o mais fundo possível por um caminho e, quando não há mais vizinhos novos, volta.
- **Estrutura:** pilha feita com um vetor e um índice `topo`.
- `proximo[u]` guarda qual vizinho de `u` ainda falta olhar, então cada aresta é vista uma vez.
- Não usa recursão, que estoura a pilha do Java em grafos com milhares de vértices.
- **Mostra:** ordem de visita, árvore DFS, vértices não alcançados e becos sem saída.

### BFS — busca em largura — O(V + E)
Visita primeiro os vizinhos da origem, depois os vizinhos dos vizinhos, e assim por diante.
- **Estrutura:** fila feita com um vetor e dois índices (`inicio` e `fim`), com entrar e sair em O(1).
- Cada vértice entra na fila uma única vez.
- `nivel[v]` é o menor número de arestas da origem até `v`.
- **Mostra:** a fila de execução, os saltos e o caminho até cada vértice.

### Dijkstra — menor caminho — O((V + E) log V)
Sempre pega o vértice ainda não fechado com a menor distância e tenta melhorar a distância dos
vizinhos dele (relaxamento).
- **Estrutura:** `MinHeap` próprio, uma árvore binária guardada em vetor. A menor distância fica na
  raiz; os filhos da posição `i` ficam em `2i+1` e `2i+2`.
- Inserir e remover custam O(log V), porque só percorrem a altura da árvore.
- Quando uma distância melhora, o vértice é inserido de novo no heap, e a cópia antiga é ignorada
  quando sai.
- Recusa pesos negativos, com os quais o algoritmo não funciona.
- **Mostra:** a distância e o caminho da origem até cada bairro.

### Kruskal — árvore geradora mínima — O(E log E)
Escolhe as arestas da mais barata para a mais cara, pulando as que fecham ciclo.
- **Ordenação:** as arestas entram no mesmo `MinHeap` pelo peso e saem em ordem crescente
  (heapsort), em O(E log E).
- **Ciclo:** o Union-Find guarda cada vértice num grupo. Se os dois lados da aresta já estão no
  mesmo grupo, ela fecharia um ciclo. Com união por altura e compressão de caminho, cada consulta
  custa quase O(1).
- Para assim que escolhe V-1 arestas.
- **Mostra:** cada aresta analisada (aceita ou rejeitada), as arestas escolhidas e o custo total.

## Estrutura do código (`src/`)
| Arquivo | Função |
|---|---|
| `Grafo.java` | Lista de adjacência |
| `MinHeap.java` | Heap de mínimo (Dijkstra e Kruskal) |
| `Algoritmos.java` | DFS, BFS, Dijkstra, Kruskal e Union-Find |
| `Modelo.java` | Grafo atual, entrada por texto e animação |
| `Painel2D.java` / `Painel3D.java` | Desenho 2D (com edição) e visão 3D |
| `Janela.java` / `Main.java` | Interface principal |
| `Testes.java` | Testes dos algoritmos |

## Desempenho
Grafo aleatório com 100.000 vértices e cerca de 400.000 arestas:

| Algoritmo | Tempo |
|---|---|
| BFS | ~30 ms |
| Dijkstra | ~70 ms |
| Kruskal | ~260 ms |
| DFS | ~140 ms |

## Observações
- Kruskal em grafo direcionado trata as arestas como não-direcionadas. Em grafo desconexo, devolve
  a floresta geradora mínima e avisa.
- Ao marcar "Direcionado" num grafo já desenhado, cada aresta vira ida e volta.
- Aresta de um vértice para ele mesmo (laço) não é aceita.
- Aresta de um vértice para ele mesmo (laço) não é aceita.
