package java_bootcamp;

import java_examples.ArtContract;
import java_examples.ArtState;
import net.corda.core.contracts.*;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class TokenContract implements Contract{
    public static String ID = "java_bootcamp.TokenContract";

    public interface Commands extends CommandData {
        class Issue implements Commands { }
    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandWithParties<TokenContract.Commands> command = requireSingleCommand(tx.getCommands(), TokenContract.Commands.class);

        if (command.getValue() instanceof TokenContract.Commands.Issue) {
            if(tx.getInputStates().size() != 0) throw new IllegalArgumentException("Command must have no input");
            if(tx.getOutputStates().size() != 1) throw new IllegalArgumentException("Command must have one output");
            if (tx.outputsOfType(TokenState.class).size() != 1) throw new IllegalArgumentException("Art transfer output should be an ArtState.");

            final TokenState tokenStateInput = tx.outputsOfType(TokenState.class).get(0);
            if(tokenStateInput.getAmount() <= 0) throw new IllegalArgumentException("Command amount is negative.");
            if(tokenStateInput.getIssuer().equals(tokenStateInput.getOwner())) throw new IllegalArgumentException("Issuer and owner MUST not be the same Party.");

            final List<PublicKey> signers = command.getSigners();
            if(!signers.contains(tokenStateInput.getIssuer().getOwningKey())) throw new IllegalArgumentException("Issuer MUST sign the command.");
        }else{
            throw new IllegalArgumentException("Unknown command.");
        }
    }
}