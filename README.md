# SmartNet Analyzer

- **Curso:** Engenharia de Computação/Software, IBMEC
- **Disciplina:** Análise e Complexidade de Algoritmos (IBM3121), 2026.2
- **Professor:** Cassius Figueiredo
- **Integrantes (nome e matrícula):**
  - Thiago Corrêa Brandão — 202301135265
  - Gabriel Franklin Monteiro — 202301135052
  - João Gabriel Rodriguez — 202307545333
  - Ian Esteves Amaral Francisco — 202608444358

## Sobre
Aplicação desktop em **Java (Swing)**, sem dependências externas, que modela uma cidade como grafo e executa DFS, BFS, Dijkstra e Kruskal. Entrada e saída são visuais: o grafo é desenhado no quadro (2D ou 3D) e os resultados são destacados nele, além de tabelas.

## Como executar

Requisito: JDK 17 ou superior (`javac -version`).

**Windows (PowerShell), na raiz do repositório:**
```powershell
javac -encoding UTF-8 -d out (Get-ChildItem src\*.java).FullName
java -cp out src.Main
```

**Windows (atalho):** dois cliques em `executar.bat`.

**Linux/macOS:**
```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out src.Main
```

Testes dos algoritmos: `java -cp out src.Testes`

## Como usar
- **Vértice:** clique no quadro. Duplo clique em um vértice para renomeá-lo.
- **Aresta:** clique na origem e depois no destino (informe o peso quando o grafo for ponderado).
- **Mover / Apagar:** reposicionam ou removem vértices e arestas.
- **Direcionado / Ponderado:** alternam o tipo do grafo a qualquer momento.
- Escolha a **origem** e clique em DFS, BFS, Dijkstra ou Kruskal. O resultado é animado no grafo e listado em texto/tabela.
- **2D / 3D:** a visão 3D (arraste para girar, roda para zoom, clique em um vértice para defini-lo como origem) mostra o mesmo grafo com os mesmos destaques. A edição é feita no 2D.
- "Carregar grafo do enunciado" insere o grafo de teste do PDF. "Apagar grafo" limpa tudo. "Finalizar aplicação" encerra o programa.

## Estrutura do código (`src/`)
| Arquivo | Função |
|---|---|
| `Grafo.java` | Lista de adjacência implementada do zero |
| `Algoritmos.java` | DFS, BFS, Dijkstra e Kruskal (com Union-Find) |
| `Modelo.java` | Estado compartilhado e animação dos passos |
| `Painel2D.java` / `Painel3D.java` | Entrada e saída visual (2D e 3D) |
| `Janela.java` / `Main.java` | Interface principal |
| `Testes.java` | Verificação dos resultados no grafo do enunciado |

## Estrutura de dados
**Lista de adjacência** (`LinkedHashMap` de vértice para lista de vizinhos), exibida na tela. Grafos urbanos são esparsos: memória O(V+E) contra O(V²) da matriz, e DFS/BFS em O(V+E).

## Complexidades
| Algoritmo | Complexidade |
|---|---|
| DFS / BFS | O(V+E) |
| Dijkstra (`PriorityQueue`, heap binário) | O((V+E) log V) |
| Kruskal (ordenação + Union-Find com rank e compressão) | O(E log E) |

## Observações
- Dijkstra rejeita pesos negativos com mensagem de erro.
- Kruskal em grafo direcionado trata as arestas como não-direcionadas; em grafo desconexo devolve a floresta geradora mínima.
- Auto-laços são ignorados.
- Por que usamos lista de adjacência e não uma matriz de adjacência para armazenar o grafo? Grafos esparsos: a lista gasta O(V+E), a matriz O(V²). A desvantagem é que checar uma aresta custa O(grau) em vez de O(1).
