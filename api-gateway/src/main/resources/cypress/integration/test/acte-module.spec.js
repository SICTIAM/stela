describe('Acte module', function () {
    before(() => {
        cy.loginTest()
        cy.visit('/sictiam')
    })


    // after(() => {
    //     cy.logout()
    // })

    it('should access acte deposit', function () {
        cy.get('.bars').click()
        cy.get('.mainMenus > :nth-child(1) .menu')
            .children()
            .should('length',4)
            .first()
            .click()
        cy.url().should('string', 'actes/nouveau')
    })

    it('should display correct errors', function () {
        cy.get('.form > :nth-child(1) > :nth-child(1) input').as('inputNumber')
        cy.get('.form > :nth-child(1) > :nth-child(2) input').as('inputObject')

        cy.get('@inputNumber').type('1245680A')
        cy.get('@inputObject').type('Acte test-e2e').clear()
        cy.get('@inputNumber').type('11{backspace}')
        cy.get('@inputObject').should('have.css', 'color').and('equal', 'rgb(159, 58, 56)')
    })

    it('should post acte', function () {
        cy.get('.form > :nth-child(1) > :nth-child(1) input').as('inputNumber')
        cy.get('.form > :nth-child(1) > :nth-child(2) input').as('inputObject')
        cy.get(':nth-child(3) > .field')
        //CAN fill all the inputs

    })

})
