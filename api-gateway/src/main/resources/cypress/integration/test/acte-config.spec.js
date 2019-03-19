describe('Acte config', function () {
    before(() => {
        cy.loginAdmin()
        cy.visit('/sictiam')
        cy.get('.right.menu button').click()
        cy.get('[href="/sictiam/admin"] > span').should((span) => {
            expect(span.text().trim()).to.be.equal('Administration')
        }).click()
        cy.get(':nth-child(1) > :nth-child(2) > .ui').click()
    })

    it('should access acte module', function () {
        cy.url().should('string', 'actes')
    })

    it('should type on input', function () {
        cy.get('#department').type('033')
        cy.get('button[type=submit]').should('disabled')
        cy.get('#department').focus().type('{backspace}{backspace}{backspace}')
        cy.get('button[type=submit]').should('not.disabled')
    })

    it('should declare guarantor agent', function () {
        cy.get('#genericProfileUuid').click()
        cy.get('#genericProfileUuid .visible').contains('admin-e2e admin-e2e').click()
        cy.get('#genericProfileUuid div.text').should('contain', 'admin-e2e admin-e2e' )
        cy.get('button[type=submit]').should('not.disabled').click()
    })

    it.only('should access acte control panel', function () {
        cy.get('.bars').click()
        cy.get(':nth-child(2) > .menu > .item').click()
        cy.get('.overlay').click()
        cy.url().should('string', 'admin/actes/parametrage-module')
        if(cy.get('#alertMessage').invoke('text') !== ''){
            cy.get(':nth-child(5) > .twelve  > .checkbox').then((input) => {
                if(input.hasClass('checked')){
                   //TODO go check if the notification is displayed
                }else{
                    //TODO go check the box
                    //TODO go check if the notification is displayed
                }
            })
        }else{
            //TODO write text then like above check or not the box and check if the notification is displayed
        }
    })

    it('should acces module actes', function () {
        cy.get('.right.menu button').click()
        cy.get('[href="/sictiam/"] > span').should((span) => {
            expect(span.text().trim()).to.be.equal('Application')
        }).click()
        cy.get('.bars').click()
        cy.get('.mainMenus > :nth-child(1) .menu')
            .children()
            .should('length',4)
            .first()
            .click()
        cy.url().should('string', 'actes/nouveau')
    })

})
