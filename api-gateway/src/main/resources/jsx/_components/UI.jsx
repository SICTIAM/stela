import React from 'react'
import { Form, Grid } from 'semantic-ui-react'

const FormField = ({ htmlFor, label, children }) =>
    <Form.Field>
        <label htmlFor={htmlFor}>{label}</label>
        {children}
    </Form.Field>

const Field = ({ htmlFor, label, children, }) =>
    <Grid>
        <Grid.Column width={4}><label htmlFor={htmlFor}>{label}</label></Grid.Column>
        <Grid.Column width={12}>{children}</Grid.Column>
    </Grid>



module.exports = { FormField, Field }