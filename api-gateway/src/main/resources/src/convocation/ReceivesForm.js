import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'

import { Button, Modal, Tab } from 'semantic-ui-react'

import StelaTable from '../_components/StelaTable'

class ReceivesForm extends Component {
	state = {
	    users: [
	        { name: 'Julie ALBALADEJO', email: 'julie.albaladejo@avisto.com' },
	        { name: 'Gérald GOLE', email: 'gérald.gole@avisto.com' },
	        { name: 'Anne-Sophie LEVEQUES', email: 'anne-sophie.leveques@sictiam.com' }
	    ]
	}
	render() {
	    const metaData = [
	        { property: 'name', displayed: true, searchable: true },
	        { property: 'email', displayed: true, searchable: true },
	    ]
	    const listContent =
			<StelaTable
			    header={false}
			    searchable={true}
			    sortable={false}
			    metaData={metaData}
			    data={this.state.users}
			/>
	    const panes = [
	        { menuItem: 'Choisir dans la liste', render: () => <Tab.Pane>{listContent}</Tab.Pane> },
	    ]
	    return (
	        <Fragment>
	            <Modal.Header>Ajouter des destinataires</Modal.Header>
	            <Modal.Content>
	                <Tab panes={panes} />
	            </Modal.Content>
	            <Modal.Actions>
	                <Button primary>
						Ajouter
	                </Button>
	                <Button red>
						Annuler
	                </Button>
	            </Modal.Actions>
	        </Fragment>
	    )
	}

}
export default translate(['api-gateway'])(ReceivesForm)