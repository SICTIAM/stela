import React from 'react'
import Dropzone from 'react-dropzone'
import { Header } from 'semantic-ui-react'

const CustomDropzone = ({ title, ...props }) => {
    const styles = {
        header: {
            color: 'rgba(102, 102, 102, 0.7)',
            userSelect: 'none',
            marginTop: '1em',
            marginBottom: '1em'
        },
        dropzone: {
            display: 'flex',
            justifyContent: 'center',
            flexDirection: 'column',
            minHeight: '10em',
            width: '100%',
            textAlign: 'center',
            borderWidth: '4px',
            borderColor: 'rgba(102, 102, 102, 0.7)',
            borderStyle: 'dashed',
            borderRadius: '8px'
        }
    }
    return (
        <Dropzone {...props} style={styles.dropzone}>
            <Header size='medium' style={styles.header}>{title}</Header>
        </Dropzone>
    )
}

export default CustomDropzone