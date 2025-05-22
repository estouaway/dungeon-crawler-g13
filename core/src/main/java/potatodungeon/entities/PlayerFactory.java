package potatodungeon.entities;

// DESIGN PATTERN: Factory + Singleton
// nao ha concorrencia real portanto o thread safe n interessa
// mas no caso se houvesse, o java garante q enums so sao inicialiados na primeira referencia
public enum PlayerFactory { //enum é thread safe
    INSTANCE;
    // downsides.. é menos flexivel, por exemplo n podemos usar herancas

    private Player currentPlayer = null;

    public Player createPlayer(float x, float y, float radius) {
        if (currentPlayer == null) {
            // sem thread safe threads diferentes iam ser null aqui e criar multiplas instancias de player
            currentPlayer = new Player(x, y, radius);
        }
        return currentPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void resetPlayer() {
        currentPlayer = null;
    }
}
