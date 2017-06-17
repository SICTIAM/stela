import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import Form from "react-jsonschema-form"

class NewPes extends Component {
    static propTypes = {
        t: PropTypes.func.isRequired
    }
    render() {
        const { t } = this.props
        const schema = {
            type: "object",
            required: ["title", "file"],
            properties: {
                title: {
                    title: "Titre",
                    type: "string"
                },
                comment: {
                    title: "Commentaire",
                    type: "string"
                },
                file: {
                    title: "Fichier",
                    type: "string",
                    format: "data-url"
                },
            }
        }
        const uiSchema = {
            title: {
                "ui:widget": "text",
                "ui:placeholder": "Titre du PES",
            },
            comment: {
                "ui:widget": "textarea",
                "ui:placeholder": "Commentaires...",
            },
            file: {
                "ui:widget": "file",
            }
        }
        const formData = {
            title: "PES validation des dépenses",
            comment: "apres avoir dépenser bien sur"
        }
        return (
            <div>
                <h1>Stela</h1>
                {t('pes.new_pes_title')}
                <Form
                    schema={schema}
                    uiSchema={uiSchema}
                    formData={formData}
                    className='ui form'
                    method="post"
                    action="/api/pes">
                    <button className="ui primary button">Envoyer </button>
                </Form>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewPes)