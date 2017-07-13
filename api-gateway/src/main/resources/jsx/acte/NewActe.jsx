import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import Form from "react-jsonschema-form"

class NewActe extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        const schema = {
            type: "object",
            required: ["number", "title", "decisionDate", "nature", "code"],
            properties: {
                number: {
                    title: "Numéro interne",
                    type: "string"
                },
                title: {
                    title: "Object de l'acte",
                    type: "string"
                },
                decisionDate: {
                    title: "Date de la décision",
                    type: "string",
                    format: "date"
                },
                nature: {
                    title: "Nature de l'acte",
                    type: "string",
                    enum: [
                        "Délibération",
                        "Actes réglementaires",
                        "Actes individuels",
                        "Contrats, conventions et avenants",
                        "Documents budgétaires et financiers",
                        "Autres"
                    ]
                },
                code: {
                    title: "Code matière de l'acte",
                    type: "string"
                },
            }
        }
        const uiSchema = {
            nature: {
                "ui:placeholder": "Choisir..."
            }
        }
        return (
            <div>
                <h1>{t('acte.new.title')}</h1>
                <Form
                    schema={schema}
                    uiSchema={uiSchema}
                    className='ui form'
                    method="post"
                    action="/api/pes">
                    <button className="ui primary button">Envoyer </button>
                </Form>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewActe)