package fr.sictiam.stela.pescommand.controller;

import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.model.ConcurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@RestController
@RequestMapping(value = "/api/pes")
public class PesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesController.class);

    private CommandBus commandBus;

    @Autowired
    public PesController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public void create(@PathVariable String id, HttpServletResponse response) {
        LOGGER.debug("Got a PES flow to create {}", id);

        try {
            CreatePesCommand createPesCommand = new CreatePesCommand(id);
            commandBus.dispatch(asCommandMessage(createPesCommand));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (AssertionError ae) {
            LOGGER.warn("Create PES failed - empty param ? '{}'", id);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (CommandExecutionException cex) {
            LOGGER.warn("Add Command FAILED with message: {}", cex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            if (null != cex.getCause()) {
                LOGGER.warn("Caused by: {} {}", cex.getCause().getClass().getName(), cex.getCause().getMessage());
                if (cex.getCause() instanceof ConcurrencyException) {
                    LOGGER.warn("A duplicate PES flow with the same ID [{}] already exists.", id);
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        }
    }
}
