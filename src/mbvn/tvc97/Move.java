package mbvn.tvc97;

/**
 * One turn of play: the human's stone and the computer's reply.
 * Kept in a history list so the turn can be undone.
 *
 * @author Tvc97
 */
class Move {

    /** Marks a turn the computer did not answer because the human had already won. */
    static final int NO_REPLY = -1;

    final int humanX, humanY;
    final int computerX, computerY;

    Move(int humanX, int humanY, int computerX, int computerY) {
        this.humanX = humanX;
        this.humanY = humanY;
        this.computerX = computerX;
        this.computerY = computerY;
    }

    boolean hasComputerReply() {
        return computerX != NO_REPLY;
    }
}
