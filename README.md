LEI24/25 - Programação e Algoritmos - Grupo 13




Checkpoint:

Componentes implementados:

Geraçao da dunageon:
 Geracao procedural que usa uma adaptacao do algoritmo binary space positioning que se adequa melhor as necessidades especificas do nosso jogo.

Optamos por um estilo de jogo com camara fixa, portanto temos sala padronizadas, com tamanho fixo e ligacoes diretas por portas, sem corredores. Usamos a estrutura logica do BSP para determiner a relacao espacial e ligacao das salas.
Esta adaptacao do BSP difere da implementação tradicional, pq n utilizamos a divisao espacial para definir o tamanho e posicao das salas, mas apenas para estabelecer uma estrutura hierárquica de organização. A arvore BSP é construida recursivamente dividindo o espaço em regioes cada vez menores, até atingirmos o número desejado de folhas terminais (8 salas). 
As ossas salas têm tamanho fixo e aparecem sempre no mesmo local da tela, centradas.
Depois usamos a estrutura de grafos para garantir a navegabilidade da dungeon a partir da estrutura bsp.

Sistema de obstaculos, temos um Sistema de geracao e obstaculos dentro da sala com varios tamanhos, garantindo q n bloqueiam portas e deteta a colisao para q o jogador n os consiga atravessar.

Foi criado um Room Helper que é basicamente um Sistema de posicionamento generico para posicionar entidades dentro das salas, aqui determinamos areas validas para colocar entidades
  -evitamos sobreposicao;
  -permite selecionamento com bias (tendencia para centro ou laterais);
  -garante acessibilidade as portas;
  -interface unificada para posicionar qq tipo de entidade;



Algoritmos:
Binary space partitioning para garantir q todas as salas estao ligadas;
Breath first search BFS verifica se todas as salas estao acessiveis a partir da sala inicial;
Algoritmo de posicionamento com restricoes;
Usamos brute force mas aleatorio em vez de testar sistematicamente todas as posicoes, verificamos aleatoriamente.


Padroes dde design:
 -factory method, utilizado para criar diferentes tipos e salas e entidades, n usamos implementacao pura com  interfaces dedicadas e sem estrutura  de heranca;
-composite, no bsp, cada nó so pode conter outros nós ou ser uma folha, mas tb n usamos interfaces comuns nem classes abstratas, temos uma estrutura em rvores mas n seguimos o padrao composite formal;
-strategy, a geracao de obstaculos podde ser visto cmo strategy mas tb n o padrao formal,temos logica condicional q altera parametros com base em outros parametros, mas n ha encapsulamento de algoritmos q pode ser trocados.



Divisao de tarefas
	
gonçalo: geracao da dungeon, estrutura e config projeto

rute: sistema de menus, ecra de pausa, interface utilizador

rui e manuel: sistema de inimigos, items, mecanicas e combate
