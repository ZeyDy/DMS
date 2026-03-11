import { BrowserRouter, Routes, Route } from "react-router-dom";
import { MainLayout } from "./layout/MainLayout";
import { HomePage } from "./pages/HomePage";
import { CustomersPage } from "./pages/CustomerPage";
import { CreateCustomerPage } from "./pages/CreateCustomerPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/customers" element={<CustomersPage />} />
          <Route path="/customers/create" element={<CreateCustomerPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;