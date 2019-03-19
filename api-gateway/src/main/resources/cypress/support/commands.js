// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.add('createAccounts', async () => {
    const admin = new FormData()
    admin.set('username', 'admin-e2e')
    admin.set('password', 'admin-e2e')

    const test = new FormData()
    test.set('username', 'test-e2e')
    test.set('password', 'test-e2e')

    await fetch('/login', {method: 'POST', body: admin})
    await fetch('/logout')
    await fetch('/login', {method: 'POST', body: test})
    await fetch('/logout')
})

Cypress.Commands.add('loginAdmin', () => {
    cy.visit('/login')
    cy.get('#username').focus().type('admin-e2e')
    cy.get('#password').focus().type('admin-e2e')
    cy.get('.btn').click()
})

Cypress.Commands.add('loginTest', () => {
    cy.visit('/login')
    cy.get('#username').focus().type('test-e2e')
    cy.get('#password').focus().type('test-e2e')
    cy.get('.btn').click()
})


Cypress.Commands.add('logout', () => {
    cy.visit('/logout')
})
