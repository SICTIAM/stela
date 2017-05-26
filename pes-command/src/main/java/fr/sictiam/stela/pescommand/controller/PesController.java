package fr.sictiam.stela.pescommand.controller;

import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import fr.sictiam.stela.pescommand.command.SendPesCommand;
import fr.sictiam.stela.pescommand.validator.PescommandValidator;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.model.ConcurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
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

    @PostMapping(value = "/new")
    public ResponseEntity<String> create(@RequestBody CreatePesCommand createPesCommand) {
        LOGGER.debug("Got a PES flow to create {} {}", createPesCommand.getId(), createPesCommand.getTitle());
        DataBinder binder = new DataBinder(createPesCommand);
        binder.setValidator(new PescommandValidator());
        binder.validate();
        BindingResult results = binder.getBindingResult();
        LOGGER.debug("Validation returned {} error(s)", results.getErrorCount());
        if (results.getErrorCount() == 0) {
            try {
                commandBus.dispatch(asCommandMessage(createPesCommand));
                return new ResponseEntity<>(HttpStatus.CREATED);
            } catch (CommandExecutionException cex) {
                LOGGER.warn("Add Command FAILED with message: {}", cex.getMessage());

                if (null != cex.getCause()) {
                    LOGGER.warn("Caused by: {} {}", cex.getCause().getClass().getName(), cex.getCause().getMessage());
                    if (cex.getCause() instanceof ConcurrencyException) {
                        LOGGER.warn("A duplicate PES flow with the same ID [{}] already exists.", createPesCommand.getId());
                        return new ResponseEntity<>(HttpStatus.CONFLICT);
                    }
                }
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            LOGGER.warn("Received PES is not valid");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value ="/pessend")
    public void pessend(@RequestBody SendPesCommand sendPesCommand, HttpServletResponse response) {
        LOGGER.debug("Got a PES send {} {} {}", sendPesCommand.getId(), sendPesCommand.getPesId(), sendPesCommand.getDateSend());


        try {
            //CreatePesCommand createPesCommand = new CreatePesCommand(id);
            commandBus.dispatch(asCommandMessage(sendPesCommand));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (AssertionError ae) {
            LOGGER.warn("Create PES failed - empty param ? '{}'", sendPesCommand.getId());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (CommandExecutionException cex) {
            LOGGER.warn("Add Command FAILED with message: {}", cex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            if (null != cex.getCause()) {
                LOGGER.warn("Caused by: {} {}", cex.getCause().getClass().getName(), cex.getCause().getMessage());
                if (cex.getCause() instanceof ConcurrencyException) {
                    LOGGER.warn("A duplicate PES flow with the same ID [{}] already exists.", sendPesCommand.getId());
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        }


    }
}
