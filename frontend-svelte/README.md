# FabxDashboard

Frontend for the FabX system.

## Dev Setup

#### Prerequisites

This project depends on the following to be installed on the system:

- NodeJS

#### Installation

Run the following command to set up the project according to the `package-lock.json`

```bash
npm ci
```

##### Automatic Linting and Formatting

The setup includes pre-commit hooks via [husky](https://typicode.github.io/husky/). On commit, husky triggers [lint-staged](https://github.com/lint-staged/lint-staged), which runs automatic linting and formatting before a commit.

## Development

```bash
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.
