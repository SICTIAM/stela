describe('PES config', ()  => {
    before(() => {
        cy.loginAdmin()
        cy.visit('/sictiam')
        cy.get('.right.menu button').click()
        cy.get('[href="/sictiam/admin"] > span').should((span) => {
            expect(span.text().trim()).to.be.equal('Administration')
        }).click()
        cy.get('.divided > :nth-child(2) > :nth-child(2) > .ui').click()
    })


    it('should access pes module', ()  => {
        cy.url().should('string', 'pes')
    })

    it('should add a SIREN', () => {
        cy.get('#sirens').type('1234567891')
        cy.get(':nth-child(3) > .twelve button').should('disabled')
        cy.get('#sirens').type('{backspace}')
        cy.get(':nth-child(3) > .twelve button').should('not.disabled').click()
        let length = 0
        cy.get(':nth-child(3) > .twelve div').then(content => {
            length = content.length

            cy.contains('123456789').within((button) => {
                cy.get('.delete').click()
            })

            cy.get(':nth-child(3) > .twelve div').should('length', length - 1 )
        })
    })

})
