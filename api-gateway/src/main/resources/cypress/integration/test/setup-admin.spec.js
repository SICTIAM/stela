export const groupName = 'e2e-test'
export const userName = 'admin-e2e'

describe('Setup admin', () => {
    beforeEach(() => {
        cy.visit('/sictiam')
        cy.get('.right.menu button').click()
        cy.get('[href="/sictiam/admin"] > span').should((span) => {
            expect(span.text().trim()).to.be.equal('Administration')
        }).click()
    })

    it('should update or create group', () => {
        cy.wait(100)
        cy.get('#content > :nth-child(1) > :nth-child(4) div[role=list] > *')
            .then(async (content) => {
                let createGroup = true
                cy.wrap(content).each(item => {
                    if (item.text() === groupName) {
                        createGroup = false
                        return false
                    }
                }).then(() => {
                    if (createGroup) {
                        cy.get(':nth-child(4) > [style="text-align: right;"] > .ui').click()
                        cy.get('#name').type(groupName)
                        selectAllOptions('#groups', '#groups > .visible')
                        cy.get('button[type=submit]').should(content => {
                            expect(content.text().trim()).equal('Créer')
                        }).click()
                        cy.get('button[type=submit]').should(content => {
                            expect(content.text().trim()).equal('Mettre à jour')
                        })
                    } else {
                        cy.contains(groupName).click()
                        selectAllOptions('#groups', '#groups > .visible')
                        cy.get('button[type=submit]').should(content => {
                            expect(content.text().trim()).equal('Mettre à jour')
                        }).click()
                    }
                })
            })
    })

    it('should add to group test-e2e the test user admin-e2e', () => {
        cy.get('#content > :nth-child(1) > :nth-child(3)').get('table').contains(userName).click()

        cy.url().should('have.string', 'agent')
        cy.wait(100)
        cy.get('.form > :nth-child(2)').then(content => {
            if (content.find('.ui.label.basic').length > 0) {
                cy.contains(groupName).should('be.visible')
            } else {
                cy.get('#groups').click()
                cy.get('#groups > .visible').contains(groupName).click()
                cy.get('button[type=submit]').click()
            }
        })
    })
})

export const selectAllOptions = (parent, childs) => {
    cy.get(parent).click()
    cy.get(childs).each((item) => {
        cy.wrap(item).click()
        if (item.text().trim()) {
            selectAllOptions(parent, childs)
        }
    })
}
