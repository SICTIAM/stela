package db.migration;

import fr.sictiam.stela.pesservice.config.SpringUtility;
import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.service.StorageService;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class V1_23__Storage_Migration extends BaseJavaMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(V1_23__Storage_Migration.class);

    @Override
    public void migrate(Context context) {
        StorageService storageService = SpringUtility.getBean(StorageService.class);
        AttachmentRepository attachmentRepository = SpringUtility.getBean(AttachmentRepository.class);

        // PesAller attachments
        Connection connection = context.getConnection();
        try (Statement select = connection.createStatement()) {
            try (ResultSet rows =
                         select.executeQuery("SELECT uuid, filename, file FROM attachment where storage_key IS NULL")) {
                while (rows.next()) {

                    String uuid = rows.getString(1);
                    String filename = rows.getString(2);
                    byte[] content = rows.getBytes(3);
                    Attachment a = new Attachment(filename, content);
                    storageService.storeAttachment(a);

                    try (Statement update = connection.createStatement()) {
                        update.execute("UPDATE attachment set storage_key='" + a.getStorageKey() + "' WHERE uuid='" + uuid + "'");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // PesHistory attachments
        try (Statement select = connection.createStatement()) {
            try (ResultSet rows =
                         select.executeQuery("SELECT uuid, file_name, file FROM pes_history where file IS NOT NULL")) {
                while (rows.next()) {

                    String uuid = rows.getString(1);
                    String filename = rows.getString(2);
                    byte[] content = rows.getBytes(3);
                    Attachment a = new Attachment(filename, content);
                    storageService.storeAttachment(a);
                    a = attachmentRepository.saveAndFlush(a);

                    try (Statement update = connection.createStatement()) {
                        update.execute("UPDATE pes_history set attachment_uuid='" + a.getUuid() + "' WHERE uuid='" + uuid + "'");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Statement drop = connection.createStatement()) {
            drop.execute("ALTER TABLE pes_history DROP file, DROP file_name");

        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Statement drop = connection.createStatement()) {
            drop.execute("ALTER TABLE attachment DROP file");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
